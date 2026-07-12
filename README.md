# Capítulo 08 — Resiliencia de la llamada síncrona

Octavo capítulo del tutorial "De cero a pro en arquitectura de microservicios con Spring Boot" (ver el índice completo de capítulos en la rama `main`). Parte directamente de `capitulo-07-http-service-client`.

## Índice

1. [Introducción](#1-introducción)
2. [Reintentos con `@Retryable`](#2-reintentos-con-retryable)
3. [Límite de concurrencia con `@ConcurrencyLimit`](#3-límite-de-concurrencia-con-concurrencylimit)
4. [Circuit breaker con Resilience4j](#4-circuit-breaker-con-resilience4j)
5. [Cómo probarlo](#5-cómo-probarlo)
   1. [Demostración manual](#51-demostración-manual)
   2. [Test de integración con `MockRestServiceServer`](#52-test-de-integración-con-mockrestserviceserver)
6. [Registro de archivos del capítulo](#6-registro-de-archivos-del-capítulo)
7. [Referencias](#7-referencias)

---

## 1. Introducción

Desde el capítulo 7, `servicio-pedidos` depende de una única llamada síncrona a `servicio-catalogo` para resolver el precio real de cada línea de un pedido. Esa llamada es, ahora mismo, un punto único de fallo (Single Point of Failure): si `servicio-catalogo` está caído, lento o responde con errores intermitentes, crear un pedido falla también — sin ningún mecanismo que lo amortigüe.

Este capítulo añade tres mecanismos de resiliencia sobre esa misma llamada, cada uno resolviendo un problema distinto:

| Mecanismo                  | Problema que resuelve |
|----------------------------|---|
| **Reintento** (Retry)      | Un fallo transitorio y puntual (un timeout suelto, un blip de red) se resuelve solo con volver a intentarlo |
| **Límite de concurrencia** | Un catálogo lento no debe agotar el pool de hilos de `servicio-pedidos` acumulando llamadas en curso |
| **Circuit breaker**        | Un fallo sostenido no se arregla insistiendo — hay que dejar de llamar y fallar rápido durante un tiempo |

Los dos primeros son nativos de Spring Framework 7, activados con una única anotación de configuración (`@EnableResilientMethods`) y aplicados método a método con `@Retryable`/`@ConcurrencyLimit`. El circuit breaker no tiene equivalente nativo en Spring Framework 7 — se cubre en una sección posterior con Resilience4j.

---

## 2. Reintentos con `@Retryable`

Antes de reintentar hace falta que el fallo se detecte en un tiempo acotado — sin eso, un `servicio-catalogo` que no está caído pero sí colgado (la conexión se establece pero nunca llega respuesta) podría dejar la llamada esperando indefinidamente, sin que `@Retryable` llegue siquiera a entrar en juego. `application.yml` fija explícitamente esos límites sobre el mismo grupo `catalogo` ya configurado en el capítulo 7:

```yaml
# servicio-pedidos/src/main/resources/application.yml
spring:
  http:
    serviceclient:
      catalogo:
        base-url: "http://localhost:8080"
        connect-timeout: 1s
        read-timeout: 2s
```

> **¿Por qué dos timeouts distintos?**
>
> `connect-timeout` acota cuánto se espera a establecer la conexión TCP — si `servicio-catalogo` está realmente caído (nadie escucha en el puerto), esto falla casi al instante, así que 1 s ya es generoso. `read-timeout` acota cuánto se espera una vez la conexión ya está establecida pero la respuesta no llega — el caso de un proceso colgado, no caído. Sin este segundo timeout explícito, esa espera no tendría un límite fijado por la aplicación.
>
> Ambos se traducen en la misma `ResourceAccessException` que ya dispara `@Retryable` (sección siguiente) y cuenta como fallo para el circuit breaker (sección 4) — son la primera línea de defensa que hace que el resto de mecanismos de este capítulo lleguen a activarse en un tiempo razonable, en vez de quedarse esperando una respuesta que nunca llega.

Spring Framework 7 incorpora un paquete propio de resiliencia (`org.springframework.resilience`), sin depender de Spring Retry ni de ninguna librería externa. Se activa declarando `@EnableResilientMethods` en una clase de configuración:

```java
// servicio-pedidos/.../infraestructura/configuracion/ResilienciaConfig.java
@Configuration
@EnableResilientMethods
public class ResilienciaConfig {
}
```

Esa anotación registra, por debajo, los *bean post-processors* que procesan tanto `@Retryable` como `@ConcurrencyLimit` (la anotación de la siguiente sección) — ambas comparten el mismo interruptor de activación, aunque se apliquen de forma independiente método a método.

Con eso activado, `@Retryable` se aplica directamente sobre el método que ya conocemos del capítulo 7, `CatalogoAdaptador.buscarProductoPorId`:

```java
// servicio-pedidos/.../infraestructura/adaptador/salida/http/adaptador/CatalogoAdaptador.java
@Retryable(
		includes = ResourceAccessException.class,
		maxRetries = 3,
		delay = 200,
		multiplier = 2,
		jitter = 50,
		maxDelay = 1500)
@Override
public ProductoCatalogoDTO buscarProductoPorId(ProductoId productoId) {
	try {
		ProductoCatalogoRespuesta respuesta = catalogoHttpExchange.buscarProductoPorId(productoId.valor());
		return new ProductoCatalogoDTO(respuesta.id(), respuesta.nombre(), respuesta.precio());
	} catch (HttpClientErrorException.NotFound excepcion) {
		throw new ProductoInexistenteException(productoId.valor());
	}
}
```

El atributo `includes` es la decisión más importante de este bloque: acota el reintento a `ResourceAccessException` — la excepción que Spring lanza ante un fallo de E/S (conexión rechazada, timeout, DNS que no resuelve), es decir, un fallo transitorio de red. Deliberadamente **no** incluye `HttpClientErrorException.NotFound` (el 404 que ya traducíamos a `ProductoInexistenteException` en el capítulo 7): reintentar un "el producto no existe" no cambia el resultado, solo retrasa una respuesta que ya es definitiva. Al no compartir jerarquía con `ResourceAccessException`, ni siquiera hace falta una exclusión explícita (`excludes`) — el `include` ya actúa como lista blanca.

> **¿Qué es exactamente `ResourceAccessException`?**
>
> Es la excepción que `RestClient` lanza cuando la llamada HTTP no llega a completarse por un problema de infraestructura de red, no de protocolo HTTP — por eso no lleva asociado ningún código de estado. Envuelve siempre una `IOException` de bajo nivel como causa: un `ConnectException` (el puerto no acepta conexiones, el proceso está caído), un `SocketTimeoutException` (la conexión se estableció pero no hubo respuesta a tiempo) o un `UnknownHostException` (el DNS no resuelve).
>
> La diferencia con `HttpClientErrorException`/`HttpServerErrorException` es la que importa aquí: esas sí implican que hubo una respuesta HTTP, aunque fuera un 404 o un 503 — el servidor contestó, solo que con un código de error. `ResourceAccessException` significa que la respuesta ni siquiera llegó a existir, que es justo el tipo de fallo transitorio que un reintento puede resolver.

El resto de atributos configura un **backoff** exponencial con **jitter**: `delay = 200` fija la espera tras el primer fallo, `multiplier = 2` la dobla en cada reintento sucesivo (200 → 400 → 800 ms), `jitter = 50` añade una variación aleatoria de ±50 ms a cada espera (evita que reintentos de peticiones distintas se sincronicen exactamente), y `maxDelay = 1500` limita cuánto puede crecer esa espera. Con `maxRetries = 3`, el total de intentos es 1 inicial + 3 reintentos = 4 — si los cuatro fallan, la excepción se propaga tal cual a quien llamó (por ahora sin traducir a un `ProblemDetail` propio; eso llega con el circuit breaker, más adelante en este mismo capítulo).

> **¿En qué unidad están `delay`, `jitter` y `maxDelay`?**
>
> En milisegundos. Los tres son un `long` plano, no un `Duration` — a diferencia de otras propiedades de configuración de Spring que sí aceptan sufijos como `500ms` o `2s`, aquí el número se interpreta directamente como milisegundos sin unidad explícita en el propio atributo.

> **¿Qué es un backoff?**
>
> Es la estrategia de ir espaciando cada reintento sucesivo con una espera mayor que la anterior, en vez de reintentar inmediatamente o siempre con la misma espera fija. La variante "exponencial" —la que usa `@Retryable` aquí— dobla esa espera en cada intento (`multiplier = 2`: 200 → 400 → 800 ms): cuanto más lleva fallando la llamada, más se espacian los intentos, dando más margen a que el problema transitorio se resuelva solo antes del siguiente intento. `maxDelay` evita que ese crecimiento sea indefinido, poniendo un techo a la espera máxima entre intentos.
>
> **¿Qué es el jitter?**
>
> Es la variación aleatoria que se añade a cada espera de un backoff, para que reintentos que fallaron a la vez no vuelvan a intentarlo también a la vez. Sin jitter, varias peticiones que fallan en el mismo instante (por ejemplo, porque `servicio-catalogo` acaba de caerse) reintentarían todas exactamente a los mismos 200 ms, luego a los mismos 400 ms... generando pequeñas oleadas sincronizadas de tráfico contra un servicio que ya está débil. Con `jitter = 50`, cada espera real fluctúa de forma aleatoria ±50 ms alrededor del valor calculado por el backoff, dispersando esos reintentos en el tiempo en vez de agruparlos.

Se puede comprobar el efecto real deteniendo `servicio-catalogo` y creando un pedido: en los logs (con `logging.level.org.springframework.resilience=TRACE`) aparecen los cuatro intentos con el backoff exacto configurado:

```
16:32:36.804  MethodRetryEvent: ...buscarProductoPorId [ResourceAccessException: I/O error on GET request...]
16:32:37.013  MethodRetryEvent: ...                                                          (+209 ms)
16:32:37.452  MethodRetryEvent: ...                                                          (+439 ms)
16:32:38.261  MethodRetryEvent: ...                                                          (+809 ms)
16:32:38.267  ERROR ... tras agotar los reintentos, se propaga ResourceAccessException
```

> **¿Por qué hay que invocar el método a través de la interfaz `CatalogoPuertoSalida` para que `@Retryable` funcione?**
>
> `@Retryable` funciona como el resto de anotaciones AOP de Spring (`@Transactional`, `@Async`): Spring envuelve el bean `CatalogoAdaptador` en un proxy que intercepta la llamada al método anotado antes de delegar en la implementación real. Ese proxy solo se interpone cuando la llamada llega desde fuera del bean — que es justo lo que ya hace `CrearPedidoServicio`, invocando `catalogoPuertoSalida.buscarProductoPorId(...)` a través de la interfaz.
>
> Si en cambio `buscarProductoPorId` se llamara desde dentro de la propia clase `CatalogoAdaptador` (`this.buscarProductoPorId(...)`, una auto-invocación), el proxy quedaría rodeado por completo: la llamada iría directa al método real sin pasar por la lógica de reintento. En este capítulo no ocurre, pero es la razón por la que llamar siempre a través del puerto de salida no es solo una cuestión de estilo hexagonal, sino un requisito técnico para que `@Retryable`/`@ConcurrencyLimit` lleguen a activarse.

---

## 3. Límite de concurrencia con `@ConcurrencyLimit`

El reintento de la sección anterior protege frente a un fallo puntual, pero no frente a otro escenario distinto: `servicio-catalogo` respondiendo *lento* en vez de fallando. Si llegan muchas creaciones de pedido a la vez y cada una se queda esperando esa respuesta lenta, las llamadas concurrentes hacia el catálogo se acumulan sin límite — y con ellas, los hilos de `servicio-pedidos` que las esperan. El **Límite de concurrencia** (Concurrency Limit) — una variante del patrón clásico Bulkhead (compartimento estanco) — acota cuántas invocaciones de un método pueden estar en curso a la vez, para que un catálogo lento agote su propia capacidad, no la de quien lo llama.

Se aplica con `@ConcurrencyLimit`, apilada junto a `@Retryable` sobre el mismo método:

```java
// servicio-pedidos/.../infraestructura/adaptador/salida/http/adaptador/CatalogoAdaptador.java
@ConcurrencyLimit(limit = 2, policy = ConcurrencyLimit.ThrottlePolicy.REJECT)
@Retryable(
		includes = ResourceAccessException.class,
		maxRetries = 3,
		delay = 200,
		multiplier = 2,
		jitter = 50,
		maxDelay = 1500)
@Override
public ProductoCatalogoDTO buscarProductoPorId(ProductoId productoId) {
	// ... sin cambios
}
```

`limit = 2` es deliberadamente bajo para que la demostración de este capítulo (sección de pruebas) no necesite lanzar decenas de peticiones simultáneas; en un servicio real se calibraría según la capacidad conocida de `servicio-catalogo`. El atributo `policy` decide qué pasa con la invocación número 3 en adelante mientras las 2 primeras siguen en curso:

- `BLOCK` (el valor por defecto de `@ConcurrencyLimit`): la invocación de más se queda esperando a que se libere una plaza — sigue ocupando un hilo de `servicio-pedidos`, solo que esperando en el semáforo interno en vez de esperando la respuesta de red. No resuelve el problema de fondo si la carga siguiera creciendo.
- `REJECT`: la invocación de más falla inmediatamente con `InvocationRejectedException`, sin esperar nada. Es la opción elegida aquí — en una ruta síncrona de petición HTTP, fallar rápido y que quien llama decida si reintentar más tarde es preferible a acumular hilos bloqueados esperando turno.

`InvocationRejectedException` (del propio paquete `org.springframework.resilience`, no una excepción de dominio) se traduce en `ControladorErroresGlobal` al mismo `ProblemDetail` que ya conocemos, con un nuevo código `503`:

```java
// servicio-pedidos/.../infraestructura/adaptador/entrada/rest/ControladorErroresGlobal.java
@ExceptionHandler(InvocationRejectedException.class)
public ProblemDetail manejarCatalogoSaturado(InvocationRejectedException excepcion) {
	ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
			"El catálogo tiene demasiadas peticiones en curso ahora mismo; inténtalo de nuevo en unos segundos");
	problema.setType(TIPO_CATALOGO_SATURADO);
	problema.setTitle("Catálogo saturado");
	return problema;
}
```

Este manejador traduce directamente una excepción de framework, no una excepción de dominio propia — igual que ya hace el `@ExceptionHandler(IllegalArgumentException.class)` de este mismo controlador desde el capítulo 6. No hace falta envolverla en un tipo de dominio: `InvocationRejectedException` no representa una regla de negocio violada, sino un límite de infraestructura, y `ControladorErroresGlobal` ya es la capa que traduce ese tipo de fallos a una respuesta HTTP.

Disparando 3 peticiones concurrentes con `servicio-catalogo` parado (para que las 2 primeras agoten su ciclo completo de reintentos) se observa el comportamiento real:

```
req1: status=503  tiempo=1.37s   (agotó los 4 intentos de @Retryable, ocupando su plaza todo ese tiempo)
req2: status=503  tiempo=1.46s   (igual que req1 — la 2ª plaza disponible)
req3: status=503  tiempo=0.12s   (rechazada al instante — sin plaza libre, ni siquiera lo intenta)
```

`req1` y `req2` devuelven `503` porque, tras agotar sus reintentos, la sección 4 añade un manejador para esa excepción — sin el circuit breaker de esa sección, hoy devolverían un `500` genérico sin `ProblemDetail`. `req3` ya devuelve `503` desde esta misma sección, con un `type` distinto (`catalogo-saturado` en vez de `catalogo-no-disponible`): una cosa es que el catálogo esté agotando reintentos, y otra distinta que sea `servicio-pedidos` quien se autolimite.

> **¿La plaza de concurrencia se libera entre reintentos, o se mantiene ocupada durante todo el ciclo?**
>
> Se mantiene ocupada durante todo el ciclo — la prueba anterior lo confirma: `req1` y `req2` tardan lo mismo que un ciclo completo de reintentos (~1,4 s, igual que en la sección 2), no lo que tardaría un único intento. Eso significa que `@ConcurrencyLimit` envuelve a `@Retryable`, no al revés: una plaza cubre la llamada externa completa — incluidos sus reintentos — y no se libera hasta que esa llamada, con todos sus intentos, termina. Se retoma con más detalle en la sección 5, al combinar los tres mecanismos del capítulo.

---

## 4. Circuit breaker con Resilience4j

Los reintentos ayudan con un fallo puntual, pero no con uno sostenido: si `servicio-catalogo` lleva caído varios minutos, seguir reintentando cada petición de creación de pedido no lo arregla — solo añade ~1,4 s de latencia inútil a cada intento, sin cambiar el resultado. El **Circuit Breaker** resuelve esto con una máquina de estados de tres posiciones:

- `CLOSED` (cerrado): estado normal — las llamadas pasan y se contabiliza su resultado.
- `OPEN` (abierto): tras detectar demasiados fallos recientes, deja de intentar la llamada real — falla al instante durante una ventana de tiempo configurada.
- `HALF_OPEN` (semiabierto): pasada esa ventana, deja pasar unas pocas llamadas de prueba para comprobar si el servicio se ha recuperado — si tienen éxito, vuelve a `CLOSED`; si no, vuelve a `OPEN`.

![Máquina de estados del circuit breaker "catalogo": CLOSED, OPEN y HALF_OPEN, con los disparadores de configuración de cada transición y la evidencia real (timestamps y log) capturada al probarlo](docs/images/capitulo-08/circuit-breaker-estados.png)

*Máquina de estados del circuit breaker `catalogo` — cada transición anotada con su disparador de configuración real (`failure-rate-threshold`, `wait-duration-in-open-state`, resultado de la llamada de prueba) y con los timestamps observados en la prueba encadenada de la sección 5.*

<br>

Spring Framework 7 no incluye esta máquina de estados de forma nativa — `@Retryable`/`@ConcurrencyLimit` no la necesitan porque resuelven problemas distintos. Aquí entra **Resilience4j**, una librería de resiliencia específica para esto, en vez de Spring Cloud Circuit Breaker: ese proyecto se declara oficialmente *superseded* por las nuevas capacidades nativas de Spring Framework 7 y sin más desarrollo previsto — el mismo caso que ya llevó a descartar OpenFeign en el capítulo 7.

Se añade con el BOM de Resilience4j en el `pom.xml` raíz (mismo patrón que `spring-cloud-dependencies`/`testcontainers-bom`):

```xml
<!-- pom.xml (raíz) -->
<dependency>
	<groupId>io.github.resilience4j</groupId>
	<artifactId>resilience4j-bom</artifactId>
	<version>${resilience4j.version}</version>
	<type>pom</type>
	<scope>import</scope>
</dependency>
```

Y en `servicio-pedidos/pom.xml`:

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-aspectj</artifactId>
</dependency>
<dependency>
	<groupId>io.github.resilience4j</groupId>
	<artifactId>resilience4j-spring-boot3</artifactId>
</dependency>
```

> **¿Por qué `spring-boot-starter-aspectj` y no `spring-boot-starter-aop`, que es como se llama en la documentación de Resilience4j?**
>
> `@CircuitBreaker` se procesa con un `@Aspect` de Spring AOP (no con `@Retryable`/`@ConcurrencyLimit`, que son proxies gestionados directamente por Spring, sin pasar por `@EnableAspectJAutoProxy`) — necesita el starter que trae `spring-aop` + `aspectjweaver`. Spring Boot 4 renombró ese starter de `spring-boot-starter-aop` a `spring-boot-starter-aspectj`; la documentación oficial de Resilience4j, escrita para Spring Boot 3, todavía referencia el nombre antiguo. Es el mismo tipo de cambio de nombre que ya vimos en el capítulo 7 con `-webmvc`/`-restclient`.

La anotación se aplica sobre el mismo método, junto a las dos ya vistas:

```java
// servicio-pedidos/.../infraestructura/adaptador/salida/http/adaptador/CatalogoAdaptador.java
@CircuitBreaker(name = "catalogo", fallbackMethod = "catalogoNoDisponible")
@ConcurrencyLimit(limit = 2, policy = ConcurrencyLimit.ThrottlePolicy.REJECT)
@Retryable(
		includes = ResourceAccessException.class,
		maxRetries = 3,
		delay = 200,
		multiplier = 2,
		jitter = 50,
		maxDelay = 1500)
@Override
public ProductoCatalogoDTO buscarProductoPorId(ProductoId productoId) {
	// ... sin cambios
}

private ProductoCatalogoDTO catalogoNoDisponible(ProductoId productoId, CallNotPermittedException excepcion) {
	throw new CatalogoNoDisponibleException(productoId.valor());
}
```

`fallbackMethod` señala, por nombre, un **método de reserva** (Fallback Method) en la misma clase: cuando el circuito está abierto, Resilience4j lanza `CallNotPermittedException` en vez de intentar la llamada real, y esa excepción se dirige automáticamente a `catalogoNoDisponible` en vez de propagarse tal cual. Aquí se traduce a una nueva excepción de dominio, `CatalogoNoDisponibleException`, con su propio manejador en `ControladorErroresGlobal` (`503`, distinto del `catalogo-saturado` de la sección 3 — un circuito abierto y una autolimitación de concurrencia son cosas distintas, aunque ambas acaben en `503`).

La configuración vive en `application.yml`, igual que ya hicimos con la URL base del cliente HTTP en el capítulo 7:

```yaml
# servicio-pedidos/src/main/resources/application.yml
resilience4j:
  circuitbreaker:
    instances:
      catalogo:
        sliding-window-size: 4
        minimum-number-of-calls: 4
        failure-rate-threshold: 50
        wait-duration-in-open-state: 5s
        permitted-number-of-calls-in-half-open-state: 1
        ignore-exceptions:
          - com.javacadabra.tienda.pedidos.dominio.excepcion.ProductoInexistenteException
          - org.springframework.resilience.InvocationRejectedException
```

Uno a uno:

- `sliding-window-size`: **de cuántas llamadas se acuerda** Resilience4j. Imagínalo como una cola de capacidad fija de `4` casillas donde se apila el resultado (éxito/fallo) de cada llamada; al llegar una quinta llamada, la más antigua se descarta para dejar sitio. Esta cola es lo único que se usa para calcular la tasa de fallo — nunca mira más atrás.
- `minimum-number-of-calls`: **a partir de cuántas llamadas** Resilience4j se atreve a calcular esa tasa de fallo. Con solo 1 llamada registrada, un único fallo daría una tasa de fallo del 100 %, pero un caso no es una muestra fiable de que `servicio-catalogo` esté realmente caído — así que hasta no reunir `4` llamadas en la cola, Resilience4j ni siquiera calcula el porcentaje, y el circuito no puede abrirse por mucho que fallen las primeras llamadas.
- `failure-rate-threshold`: el umbral, en porcentaje, que dispara la apertura del circuito una vez cumplido `minimum-number-of-calls`. Aquí, `50` — en cuanto la mitad o más de las llamadas de la cola son fallos, `CLOSED` pasa a `OPEN`.
- `wait-duration-in-open-state`: cuánto tiempo se queda el circuito en `OPEN`, fallando al instante sin ni siquiera intentar la llamada real, antes de darle a `servicio-catalogo` una oportunidad de demostrar que se ha recuperado. Aquí, `5s`.
- `permitted-number-of-calls-in-half-open-state`: pasada esa espera, cuántas llamadas de prueba deja pasar el circuito en `HALF_OPEN` para decidir si vuelve a `CLOSED` o si vuelve a `OPEN`. Aquí, `1` — basta con que esa única llamada de prueba tenga éxito para cerrar el circuito otra vez, o con que falle para reabrirlo.
- `ignore-exceptions`: qué excepciones no cuentan como "fallo" a efectos de la cola, aunque la llamada termine en error — se explica en detalle más abajo, es la decisión más importante de este bloque.

`sliding-window-size` y `minimum-number-of-calls` son los dos que más se prestan a confusión, porque aquí comparten el mismo valor. Responden a preguntas distintas — uno es la memoria, el otro el arranque —, y con la configuración de este capítulo, `4` y `4`, el rastro sería así:

```
Llamada 1 (falla): cola = [F]          → 1 de 4 registradas, aún sin evaluar
Llamada 2 (falla): cola = [F,F]        → 2 de 4 registradas, aún sin evaluar
Llamada 3 (falla): cola = [F,F,F]      → 3 de 4 registradas, aún sin evaluar
Llamada 4 (falla): cola = [F,F,F,F]    → 4 de 4 registradas → ya se evalúa: 4/4 = 100 % ≥ 50 % → CLOSED pasa a OPEN
```

Que ambos valgan lo mismo no es casualidad: significa que la primera evaluación posible coincide exactamente con el momento en que la cola se llena por primera vez, sin ningún hueco intermedio. Si `minimum-number-of-calls` fuera menor que `sliding-window-size` (p. ej. `2` con una ventana de `4`), Resilience4j empezaría a evaluar antes de que la cola llegara a llenarse — el circuito podría abrirse ya con solo 2 llamadas fallidas, sin esperar a las otras 2 casillas.

`ignore-exceptions` es la decisión más importante de este bloque, con el mismo espíritu que el `includes` de `@Retryable` en la sección 2: un `404` legítimo (`ProductoInexistenteException` — el producto de verdad no existe) no es una señal de que el catálogo esté fallando, y un rechazo por saturación propia (`InvocationRejectedException` — nuestra propia sección 3 autolimitándose) tampoco lo es. Sin excluirlas, ambas contarían como "fallos" en la ventana de Resilience4j y podrían abrir el circuito por motivos que nada tienen que ver con la salud real de `servicio-catalogo`.

### El circuit breaker envuelve cada intento, no la llamada completa

Resilience4j envuelve cada **intento individual** de `@Retryable`, no la llamada externa completa — una precisión clave a la hora de diseñar el `fallback` de la sección anterior. Por eso solo existe un método de `fallback`, para `CallNotPermittedException` (circuito abierto): un segundo `fallback` para `ResourceAccessException`, pensado para traducir a un `ProblemDetail` propio el fallo de "reintentos agotados con el circuito aún cerrado", rompería los reintentos de la sección 2. En cuanto el primer intento fallara con `ResourceAccessException`, Resilience4j lo interceptaría y lo convertiría en `CatalogoNoDisponibleException` antes de que `@Retryable` pudiera verlo — y como esa excepción no está en su `includes`, `@Retryable` abortaría sin reintentar. El `fallback` del circuit breaker se "comería" la excepción que los reintentos necesitan ver.

Por eso `ResourceAccessException` se deja propagar sin traducir mientras el circuito está cerrado: así `@Retryable` la ve tal cual y reintenta con normalidad. Si los 4 intentos se agotan sin que el circuito llegue a abrirse, la excepción llega intacta hasta `ControladorErroresGlobal`, que la traduce directamente (sin pasar por Resilience4j):

```java
// servicio-pedidos/.../infraestructura/adaptador/entrada/rest/ControladorErroresGlobal.java
@ExceptionHandler(ResourceAccessException.class)
public ProblemDetail manejarCatalogoNoDisponiblePorFallosRepetidos(ResourceAccessException excepcion) {
	ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
			"El catálogo no responde tras agotar los reintentos");
	problema.setType(TIPO_CATALOGO_NO_DISPONIBLE);
	problema.setTitle("Catálogo no disponible");
	return problema;
}
```

Esto también explica por qué el circuito puede abrirse **dentro de una sola petición** del cliente: con `minimum-number-of-calls: 4`, los 4 intentos internos de una única llamada de `servicio-pedidos` ya bastan para llenar la ventana de Resilience4j y, si los 4 fallan, abrir el circuito antes de que esa misma petición termine de responder. `sliding-window-size`/`minimum-number-of-calls` cuentan intentos de bajo nivel, no peticiones HTTP de quien llama a la API.

Prueba real, encadenada:

```
Petición 1 (18:49:05, circuito CLOSED):     4 intentos con backoff real (~1,57 s) → los 4 fallan → CLOSED pasa a OPEN
                                             → responde 503 "Catálogo no disponible" (vía ResourceAccessException)
Petición 2 (18:49:13, circuito OPEN):       pasados >5 s, el circuito ya está en HALF_OPEN al llegar la petición
                                             → la llamada de prueba falla (catálogo aún caído) → HALF_OPEN vuelve a OPEN al instante (2 ms)
                                             → responde 503 "Catálogo no disponible" (vía CatalogoNoDisponibleException, con productoId)
[...servicio-catalogo se levanta, todavía sin el producto de prueba creado...]
Petición 3 (18:50:07, circuito OPEN):       pasados otros >5 s, el circuito pasa a HALF_OPEN de nuevo
                                             → la llamada de prueba llega de verdad al catálogo, pero el producto no existe (404)
                                             → ProductoInexistenteException, ignorada por ignore-exceptions: ni cuenta como fallo
                                               ni cierra el circuito — se queda en HALF_OPEN
[...se crea un producto real en servicio-catalogo...]
Petición 4 (18:59:08, circuito HALF_OPEN):  la llamada de prueba tiene éxito (24 ms) → HALF_OPEN pasa a CLOSED
                                             → responde 201 (pedido creado con normalidad)
```

La petición 3 deja una precisión interesante sobre `permitted-number-of-calls-in-half-open-state`: una excepción ignorada no cuenta como una de esas llamadas de prueba permitidas — ni a favor ni en contra —, así que el circuito se queda esperando en `HALF_OPEN` una llamada que sí se evalúe de verdad, por mucho tiempo que pase entre medias (aquí, más de 8 minutos).

Y en los logs, la secuencia completa de transiciones de estado:

```
CircuitBreaker 'catalogo' exceeded failure rate threshold. Current failure rate: 100.0
CircuitBreaker 'catalogo' changed state from CLOSED to OPEN
CircuitBreaker 'catalogo' changed state from OPEN to HALF_OPEN
CircuitBreaker 'catalogo' changed state from HALF_OPEN to OPEN
CircuitBreaker 'catalogo' changed state from OPEN to HALF_OPEN
CircuitBreaker 'catalogo' changed state from HALF_OPEN to CLOSED
```

---

## 5. Cómo probarlo

Hay dos formas de comprobar todo lo anterior: una demostración manual (parando y levantando `servicio-catalogo` a mano, como en el resto del capítulo) y un test de integración que fija ese comportamiento de forma determinista, sin depender de parar procesos reales.

### 5.1. Demostración manual

Con `servicio-catalogo` **parado**, arranca `servicio-pedidos` con el log de resiliencia en `TRACE` para ver reintentos y transiciones del circuito en los logs:

```bash
./mvnw -pl servicio-pedidos spring-boot:run -Dspring-boot.run.arguments=--logging.level.org.springframework.resilience=TRACE
```

> **¿Por qué estos `curl` no muestran el cuerpo de la respuesta?**
>
> A diferencia de los `curl` de capítulos anteriores, aquí lo único que interesa para observar la resiliencia es el código HTTP y cuánto ha tardado la petición — no el JSON devuelto. Tres flags nuevas lo consiguen:
>
> - `-s` (silent): no mostrar la barra de progreso de `curl`.
> - `-o /dev/null`: descartar el cuerpo de la respuesta.
> - `-w "%{http_code} (%{time_total}s)\n"` (write-out): en vez del cuerpo, imprimir esta plantilla con dos variables que `curl` rellena al terminar la petición — `%{http_code}` (el código HTTP) y `%{time_total}` (segundos totales, con decimales).

Cada creación de pedido reintenta y acaba fallando:

```bash
curl -s -o /dev/null -w "%{http_code} (%{time_total}s)\n" -X POST http://localhost:8081/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{"clienteId":"3fa85f64-5717-4562-b3fc-2c963f66afa6","lineas":[{"productoId":"3fa85f64-5717-4562-b3fc-2c963f66afaa","cantidad":1}]}'
# 503 (1.55s) — 4 intentos con backoff antes de responder "Catálogo no disponible";
# con minimum-number-of-calls=4, esos mismos 4 intentos ya bastan para abrir el circuito.
```

Repitiendo la misma petición justo después, el circuito ya está abierto — la respuesta llega en un instante (sin reintentar):

```bash
curl -s -o /dev/null -w "%{http_code} (%{time_total}s)\n" -X POST http://localhost:8081/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{"clienteId":"3fa85f64-5717-4562-b3fc-2c963f66afa6","lineas":[{"productoId":"3fa85f64-5717-4562-b3fc-2c963f66afaa","cantidad":1}]}'
# 503 (0.24s) — CallNotPermittedException -> catalogoNoDisponible, sin tocar la red
```

Lanzando 3 peticiones concurrentes (`limit = 2` en `@ConcurrencyLimit`) se ve el rechazo por saturación, distinto del anterior — pero aquí `%{http_code}` no sirve: los dos errores posibles (circuito abierto y saturación por concurrencia) responden el mismo `503`. La única forma de distinguirlos es el propio cuerpo de la respuesta (el campo `title` del `ProblemDetail`), así que esta vez sí lo conservamos — sin `-o /dev/null` — y lo extraemos con `jq`:

```bash
for i in 1 2 3; do
  curl -s -X POST http://localhost:8081/api/pedidos \
    -H "Content-Type: application/json" \
    -d '{"clienteId":"3fa85f64-5717-4562-b3fc-2c963f66afa6","lineas":[{"productoId":"3fa85f64-5717-4562-b3fc-2c963f66afaa","cantidad":1}]}' \
    | jq -r --arg i "$i" '"req\($i): " + .title' &
done
wait
# Dos peticiones: "Catálogo no disponible" (circuito ya OPEN) — una: "Catálogo saturado" (InvocationRejectedException)
# El orden de las tres líneas en la consola varía entre ejecuciones: se lanzan en paralelo con `&`.
```

> **¿Y esas líneas con corchetes tipo `[2] 61154 61155`?**
>
> Son del propio shell (aquí `zsh`), no de `curl` ni de `jq`: cada vez que se lanza un proceso en segundo plano con `&` se le asigna un número de trabajo y se muestran sus PID — dos en este caso, uno por cada lado de la tubería `curl | jq`. Al terminar, el shell imprime además una línea `[N]  + done  <comando>` por cada trabajo. Es ruido de control de trabajos intercalado con la salida real (`req1: ...`, `req2: ...`, `req3: ...`); se puede ignorar sin más, y no aparecería si el bucle se ejecutara desde un script en vez de pegarlo directamente en una sesión interactiva. Una ejecución real tiene este aspecto:
>
> ```
> [2] 61154 61155
> [3] 61156 61157
> [4] 61158 61159
> req3: Catálogo saturado
> [4]  + done       curl -s -X POST http://localhost:8081/api/pedidos -H  -d  | jq -r --arg i "$i" '"req\($i): " + .title'
> req2: Catálogo no disponible
> [3]  + done       curl -s -X POST http://localhost:8081/api/pedidos -H  -d  | jq -r --arg i "$i" '"req\($i): " + .title'
> req1: Catálogo no disponible
> [2]  + done       curl -s -X POST http://localhost:8081/api/pedidos -H  -d  | jq -r --arg i "$i" '"req\($i): " + .title'
> ```
>
> Las tres líneas que importan son `req3: Catálogo saturado`, `req2: Catálogo no disponible` y `req1: Catálogo no disponible` — el resto es control de trabajos del shell.

Y levantando `servicio-catalogo` (con al menos un producto creado) y esperando los `wait-duration-in-open-state: 5s` configurados, la siguiente petición ya se sirve con normalidad — la llamada de prueba en `HALF_OPEN` tiene éxito y cierra el circuito. Al contrario que en los `curl` anteriores, aquí lo interesante ya no es el código HTTP ni el tiempo, sino el propio pedido creado — así que en vez de descartarlo lo extraemos con `jq`, igual que en la petición concurrente:

```bash
curl -s -X POST http://localhost:8081/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{"clienteId":"3fa85f64-5717-4562-b3fc-2c963f66afa6","lineas":[{"productoId":"<productoId-real>","cantidad":2}]}' \
  | jq -r '"pedido creado: " + .id + " (total: " + (.total | tostring) + ")"'
# pedido creado: 3fa85f64-5717-4562-b3fc-2c963f66afab (total: 39.98)
# Si jq consigue parsear un PedidoDTO válido, la petición ha respondido 201 — no hace falta comprobar el código aparte.
```

### 5.2. Test de integración con `MockRestServiceServer`

La demostración manual depende de parar procesos y de la sincronización entre ventanas de tiempo — útil para verlo una vez, pero no para repetirlo en cada build. `CatalogoAdaptadorResilienciaTest` fija el mismo comportamiento con `MockRestServiceServer` (de `spring-test`, ya presente en el proyecto — sin dependencias nuevas), simulando fallos de red deterministas en vez de depender de que `servicio-catalogo` esté realmente caído.

> **¿Qué es exactamente `MockRestServiceServer`?**
>
> Es una clase de `spring-test` que sustituye el transporte HTTP real de un `RestClient` (o `RestTemplate`) por uno falso, a un nivel lo bastante bajo (el `ClientHttpRequestFactory`) para que el código bajo test no note la diferencia: sigue llamando a `catalogoPuertoSalida.buscarProductoPorId(...)` exactamente igual, pero esa llamada nunca abre un socket real — la responde el propio `MockRestServiceServer` según lo que le hayamos configurado. Es el mismo principio que un `@Mock` de Mockito, pero operando al nivel de HTTP en vez de al nivel de objeto Java.

El reto técnico es interceptar el `RestClient` real que usa `CatalogoHttpExchange` — el mismo que construye `@ImportHttpServices` a partir de `spring.http.serviceclient.catalogo.base-url`, no uno nuevo creado a mano. `RestClientHttpServiceGroupConfigurer` (Spring Framework 7) da acceso exactamente a ese `RestClient.Builder` por nombre de grupo, antes de que se construya el cliente final:

```java
// servicio-pedidos/.../infraestructura/adaptador/salida/http/adaptador/CatalogoAdaptadorResilienciaTest.java
@TestConfiguration
static class ConfiguracionMockCatalogo {

	private MockRestServiceServer mockServer;

	@Bean
	RestClientHttpServiceGroupConfigurer interceptarGrupoCatalogo() {
		return groups -> groups.filterByName("catalogo")
				.forEachClient((group, builder) -> mockServer = MockRestServiceServer.createServer(builder));
	}

	MockRestServiceServer mockServer() {
		return mockServer;
	}
}
```

Desglosado línea a línea:

- `@TestConfiguration static class ConfiguracionMockCatalogo`: una clase de configuración anidada y **solo para tests** — Spring Boot la detecta automáticamente por estar anidada dentro de la clase de test y añade sus beans al contexto, sin necesidad de declararla en ningún sitio más.
- `interceptarGrupoCatalogo()`: un `@Bean` que devuelve una función (`RestClientHttpServiceGroupConfigurer` es una interfaz funcional). Spring Framework 7 invoca automáticamente **todo** bean de este tipo mientras construye los grupos de `@ImportHttpServices`, pasándole la lista completa de grupos — de ahí que haga falta `filterByName("catalogo")` para quedarnos solo con el grupo de este capítulo (`servicio-pedidos` podría tener más grupos en el futuro).
- `forEachClient((group, builder) -> ...)`: por cada cliente de ese grupo, recibe su `RestClient.Builder` **antes de que se construya el cliente final** — es el único momento en el que se puede sustituir su transporte.
- `MockRestServiceServer.createServer(builder)`: esta es la línea clave. Engancha el mock al `RestClient.Builder` real (sustituyendo su `ClientHttpRequestFactory` por uno falso) y **devuelve el propio `MockRestServiceServer`**, que guardamos en el campo `mockServer` para usarlo desde los tests.
- `MockRestServiceServer mockServer()`: un getter normal, sin `@Bean` — no expone un segundo bean, solo permite leer el campo desde fuera. Por eso el test no autoinyecta un `MockRestServiceServer` directamente, sino la propia `ConfiguracionMockCatalogo`, y llama a `.mockServer()` para obtenerlo:

```java
@Autowired
private ConfiguracionMockCatalogo configuracionMockCatalogo;

private MockRestServiceServer mockServer;

@BeforeEach
void reiniciar() {
	mockServer = configuracionMockCatalogo.mockServer();
	mockServer.reset();
	circuitBreakerRegistry.circuitBreaker("catalogo").reset();
}
```

`mockServer.reset()` y `circuitBreakerRegistry...reset()` explican, de paso, la última frase de este apartado: como el contexto de Spring (con el `RestClient` ya interceptado, y el estado del circuit breaker) se reutiliza entre los métodos de test de la misma clase, hay que devolver ambos a su estado inicial antes de cada test — si no, un test heredaría el circuito ya `OPEN` de otro anterior.

Con eso, cada test simula el fallo que necesita devolviendo (o lanzando) lo que corresponda desde el propio `ResponseCreator` — incluida una `IOException` real, que `RestClient` traduce a `ResourceAccessException` exactamente igual que ante un fallo de red de verdad:

```java
@Test
void reintentaTrasFallosDeRedYFinalmentePropagaElFalloSiElCatalogoNoResponde() {
	ProductoId productoId = ProductoId.de(UUID.randomUUID().toString());
	mockServer.expect(ExpectedCount.times(4), requestTo("http://localhost:8080/api/productos/" + productoId.valor()))
			.andRespond(request -> {
				throw new IOException("Fallo de red simulado");
			});

	assertThatThrownBy(() -> catalogoPuertoSalida.buscarProductoPorId(productoId))
			.isInstanceOf(ResourceAccessException.class);

	mockServer.verify();
}
```

> **`expect`/`andRespond`/`verify`: preparar, actuar, comprobar**
>
> Este trío sigue el mismo patrón que cualquier test con un mock, solo que aplicado a peticiones HTTP en vez de a llamadas a objetos:
>
> - `mockServer.expect(...)` se ejecuta **antes** de llamar al código bajo test — no comprueba nada todavía, solo prepara una regla: "cuando llegue una petición que case con `requestTo(...)`, respóndele así". `ExpectedCount.times(4)` añade una condición extra a esa regla: tiene que casar exactamente 4 veces, ni más ni menos — es la forma en la que el test fija el número de intentos de `@Retryable` (1 inicial + 3 reintentos) que prometía la sección 2.
> - `.andRespond(request -> { throw new IOException(...); })` es la respuesta asociada a esa regla: en vez de devolver un código HTTP, lanza la excepción directamente, para que `RestClient` la traduzca a `ResourceAccessException` tal cual haría ante un fallo de red real.
> - Entre medias se llama al código real (`catalogoPuertoSalida.buscarProductoPorId(...)`) y se comprueba **su** resultado con `assertThatThrownBy(...)` — esa es la aserción sobre el comportamiento de `servicio-pedidos`.
> - `mockServer.verify()` es la aserción distinta y final: comprueba que **todas** las reglas declaradas con `expect(...)` se cumplieron exactamente como se pidió. Si `@Retryable` hubiera reintentado 3 veces en vez de 4 (o 5), `assertThatThrownBy` seguiría pasando igual — la excepción final es la misma — pero `mockServer.verify()` fallaría, porque es quien de verdad cuenta las llamadas.

El resto de tests de la clase cubren, con la misma técnica: que tras esos 4 fallos el circuito pasa a `OPEN` y una segunda llamada falla al instante con `CatalogoNoDisponibleException` (sin llegar a pedir un quinto intento al mock — prueba de que no llama de más), y que una respuesta `200` normal se traduce igual que antes de este capítulo.

## 6. Registro de archivos del capítulo

🌱 Creado · ✏️ Actualizado · 🗑️ Eliminado

### Documentación e imágenes

| | Archivo | Descripción funcional | Descripción del cambio |
|:---:|---|---|:---:|
| 🌱 | [`docs/diagramas/capitulo-08-circuit-breaker-estados.excalidraw`](docs/diagramas/capitulo-08-circuit-breaker-estados.excalidraw) | Diagrama Excalidraw (fuente editable) de la máquina de estados del circuit breaker | --- |
| 🌱 | [`docs/images/capitulo-08/circuit-breaker-estados.png`](docs/images/capitulo-08/circuit-breaker-estados.png) | Render del diagrama anterior, embebido en la [sección 4](#4-circuit-breaker-con-resilience4j) | --- |

Además, se desligó el texto de su contenedor (`containerId`) en los diagramas Excalidraw (y sus PNG) de los capítulos 1, 2, 6 y 7 — `capitulo-01-arquitectura-hexagonal`, `capitulo-01-secuencia-crear-producto`, `capitulo-02-modelo-grafo-categoria`, `capitulo-02-secuencia-recomendar-producto`, `capitulo-06-modelo-relacional-pedidos` y `capitulo-07-secuencia-crear-pedido` —, y se ajustó el tamaño de letra de un par de etiquetas para evitar que el texto se recortara. Corrige un fallo del plugin de Excalidraw para IntelliJ que dejaba ese texto invisible en el editor (sin cambios visuales en los PNG renderizados).

### Build y configuración

| | Archivo | Descripción funcional | Descripción del cambio |
|:---:|---|---|:---:|
| ✏️ | [`pom.xml`](pom.xml) | POM padre del monorepo | Añadido `resilience4j-bom` a `dependencyManagement`, con la versión centralizada en la propiedad `resilience4j.version` |
| ✏️ | [`servicio-pedidos/pom.xml`](servicio-pedidos/pom.xml) | Dependencias Maven de `servicio-pedidos` | Añadidas `spring-boot-starter-aspectj` (necesaria para el `@Aspect` de Resilience4j) y `resilience4j-spring-boot3` |
| ✏️ | [`servicio-pedidos/src/main/resources/application.yml`](servicio-pedidos/src/main/resources/application.yml) | Configuración de `servicio-pedidos` | Añadidos `connect-timeout`/`read-timeout` al grupo `catalogo` del capítulo 7, y la instancia `resilience4j.circuitbreaker.instances.catalogo` (ventana, umbral de fallos, tiempo en abierto, excepciones ignoradas) |

### Dominio

| | Archivo | Descripción funcional | Descripción del cambio |
|:---:|---|---|:---:|
| 🌱 | [`.../dominio/excepcion/CatalogoNoDisponibleException.java`](servicio-pedidos/src/main/java/com/javacadabra/tienda/pedidos/dominio/excepcion/CatalogoNoDisponibleException.java) | Excepción de dominio cuando el catálogo no responde (reintentos agotados o circuito abierto) | --- |

### Infraestructura de entrada

| | Archivo | Descripción funcional | Descripción del cambio |
|:---:|---|---|:---:|
| ✏️ | [`.../infraestructura/adaptador/entrada/rest/ControladorErroresGlobal.java`](servicio-pedidos/src/main/java/com/javacadabra/tienda/pedidos/infraestructura/adaptador/entrada/rest/ControladorErroresGlobal.java) | Manejador global de excepciones de `servicio-pedidos` | Tres nuevos `@ExceptionHandler`: `InvocationRejectedException` → `503` "Catálogo saturado", `CatalogoNoDisponibleException` → `503` "Catálogo no disponible" (circuito abierto), `ResourceAccessException` → `503` "Catálogo no disponible" (reintentos agotados, circuito aún cerrado) |

### Infraestructura de salida

| | Archivo | Descripción funcional | Descripción del cambio |
|:---:|---|---|:---:|
| 🌱 | [`.../infraestructura/configuracion/ResilienciaConfig.java`](servicio-pedidos/src/main/java/com/javacadabra/tienda/pedidos/infraestructura/configuracion/ResilienciaConfig.java) | Activa `@Retryable`/`@ConcurrencyLimit` vía `@EnableResilientMethods` | --- |
| ✏️ | [`.../infraestructura/adaptador/salida/http/adaptador/CatalogoAdaptador.java`](servicio-pedidos/src/main/java/com/javacadabra/tienda/pedidos/infraestructura/adaptador/salida/http/adaptador/CatalogoAdaptador.java) | Adaptador que implementa `CatalogoPuertoSalida` sobre `CatalogoHttpExchange` | Añadidas `@Retryable`, `@ConcurrencyLimit` y `@CircuitBreaker` (con `fallbackMethod`) sobre `buscarProductoPorId` |

### Tests

| | Archivo | Descripción funcional | Descripción del cambio |
|:---:|---|---|:---:|
| 🌱 | [`.../infraestructura/adaptador/salida/http/adaptador/CatalogoAdaptadorResilienciaTest.java`](servicio-pedidos/src/test/java/com/javacadabra/tienda/pedidos/infraestructura/adaptador/salida/http/adaptador/CatalogoAdaptadorResilienciaTest.java) | Test de integración: reintentos, apertura del circuito y recuperación, con `MockRestServiceServer` | --- |

## 7. Referencias

- [Resilience (Spring Framework Reference)](https://docs.spring.io/spring-framework/reference/core/resilience.html)
- [Getting Started — Resilience4j Spring Boot 3](https://resilience4j.readme.io/docs/getting-started-3)
- [HTTP Service Groups — Customizing clients (Spring Framework Reference)](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html)
- [Testing Client Applications — `MockRestServiceServer` (Spring Framework Reference)](https://docs.spring.io/spring-framework/reference/testing/spring-mvc-test-client.html)
