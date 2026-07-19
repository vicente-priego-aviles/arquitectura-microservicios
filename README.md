# Capítulo 14 — Saga orquestada a mano

Decimocuarto capítulo del tutorial "De cero a pro en arquitectura de microservicios con Spring Boot" (ver el índice completo de capítulos en la rama `main`). Parte directamente de `capitulo-12-outbox-transaccional`.

## Índice

1. [Motivación y arquitectura general del capítulo](#1-motivación-y-arquitectura-general-del-capítulo)
2. [El comando, la respuesta y el orquestador](#2-el-comando-la-respuesta-y-el-orquestador)
3. [Cómo probarlo](#3-cómo-probarlo)
4. [Registro de archivos del capítulo](#4-registro-de-archivos-del-capítulo)
5. [Referencias](#5-referencias)

---

## 1. Motivación y arquitectura general del capítulo

Crear un pedido dispara una cadena de pasos que cruza dos microservicios. `servicio-pedidos` guarda el `Pedido` y, vía Outbox transaccional, publica `PedidoCreadoEvento` en Kafka; `servicio-inventario` lo consume y `ReservarStockServicio` decrementa el stock de cada línea. Esa cadena funciona bien mientras todo sale bien. La pregunta que este capítulo responde es la contraria. **¿Qué pasa si `reservar(...)` falla?**

`Stock.decrementar(...)` lanza `StockInsuficienteException` si la cantidad pedida supera la disponible, y `ReservarStockServicio` puede lanzar también `StockNoEncontradoException` si el producto no tiene fila de stock. Ahora mismo ese fallo se pierde. `PedidoCreadoConsumidorConfiguracion` no captura la excepción, así que sube sin más hasta el binder de Spring Cloud Stream/Kafka, que aplica su comportamiento de reintento por defecto y, agotados los intentos, se limita a loguear el error y confirmar el offset — el mensaje se da por consumido y `servicio-inventario` sigue con el siguiente sin volver a intentarlo.

Mientras tanto, `servicio-pedidos` no se entera de nada. Ya devolvió `201 Created` al cliente en cuanto `CrearPedidoServicio` guardó el `Pedido`, antes incluso de que Kafka entregara el evento — y el propio `Pedido` no tiene hoy ningún concepto de estado que permita distinguir "confirmado" de "con la reserva pendiente" o "rechazado". El pedido queda registrado en su propia base de datos como si todo hubiera ido bien, mientras que en la base de datos de `servicio-inventario` el stock nunca se decrementó. Dos bases de datos autónomas, cada una consistente *por separado*, pero **inconsistentes entre sí** respecto a un mismo hecho de negocio.

> **¿Por qué no una transacción distribuida que cubra las dos bases de datos?**
>
> Porque cada microservicio es dueño exclusivo de su propia base de datos — la misma decisión que ya se tomó en el capítulo 6 (persistencia políglota, Neo4j para `servicio-catalogo` y PostgreSQL para `servicio-pedidos`/`servicio-inventario`) y que este proyecto no se plantea revertir. Un *commit* de dos fases (2PC) exigiría que ambas bases de datos participaran del mismo protocolo de transacción, algo que ni Neo4j y PostgreSQL comparten, ni encajaría con un *broker* de mensajería como Kafka de por medio.
>
> La alternativa no es evitar la inconsistencia, sino aceptarla como transitoria y repararla con un paso más una vez se detecta — que es exactamente lo que resuelve una **Saga** (patrón para mantener la consistencia de una transacción de negocio que abarca varios microservicios, y por tanto varias bases de datos autónomas, sin una transacción distribuida clásica).

Una Saga parte una transacción de negocio en una secuencia de pasos locales, cada uno con su propia **Acción de compensación** (Compensating Action) que deshace su efecto si un paso posterior falla — no es un *rollback* de base de datos, porque cada paso ya hizo su propio *commit* local e irreversible por esa vía, sino una operación de negocio nueva. En este capítulo la Saga tiene dos pasos: **crear el pedido** (ya existente, capítulo 6) y **reservar el stock** (ya existente, capítulo 12). Si el segundo falla, la acción de compensación es cancelar el pedido del primero.

Hay dos formas de implementar una Saga. En la **Coreografía** (Choreography), ningún componente central conoce la secuencia completa. Cada microservicio reacciona a los eventos de los demás y publica los suyos propios, y el flujo entero solo existe como la suma de esas reacciones individuales — es el camino que sigue el capítulo 13 sobre este mismo escenario. En la **Orquestación** (Orchestration), la que cubre este capítulo, un componente dedicado sí conoce y dirige la secuencia completa. Llama a cada paso explícitamente y decide cuándo compensar.

Este capítulo elige orquestación y la construye **a mano** — sin ningún motor de procesos de negocio (BPMN, un *framework* de *workflow*), con un componente Java explícito. El comando y su respuesta, además, viajan por HTTP síncrono en vez de por Kafka. Reutilizan el mismo HTTP Service Client del capítulo 7 (`servicio-pedidos` ya llama así a `servicio-catalogo`) en vez de la infraestructura de mensajería de los capítulos 11 y 12. Casi nada de esa infraestructura sobrevive a este capítulo — ni el Outbox transaccional ni el evento `PedidoCreadoEvento` de `servicio-pedidos` tienen ya ningún consumidor una vez el comando pasa a ser una llamada directa, así que ambos desaparecen (ver [sección 2](#2-el-comando-la-respuesta-y-el-orquestador)). Un capítulo futuro podría delegar esta misma coordinación en un motor de orquestación dedicado; escribirla primero a mano deja ver con claridad qué problema resuelve ese motor antes de introducirlo.

El cambio de coreografía a orquestación no es solo "quién decide", sino también "qué se envía". En el capítulo 13, `servicio-inventario` reacciona a un evento de dominio ya consumado — un hecho, no una petición — y decide por su cuenta publicar su propio evento de resultado. En este capítulo, en cambio, `servicio-pedidos` le envía a `servicio-inventario` un **Comando** (Command) explícito, una instrucción dirigida en vez de un hecho anunciado, y espera de vuelta una respuesta concreta en la misma llamada. Quien decide qué pasa después, confirmar el pedido o cancelarlo, no es `servicio-inventario`, sino el orquestador dentro de `servicio-pedidos` — el único sitio del sistema donde vive el conocimiento de la secuencia completa.

El detalle de ese comando y su respuesta, el nuevo estado de `Pedido`, y la implementación del orquestador se desarrollan en las secciones siguientes.

## 2. El comando, la respuesta y el orquestador

### `Pedido` necesita estado

Hasta este capítulo, `Pedido` no tenía ningún concepto de estado — se creaba y ya está, no había nada más que distinguir. Eso ya no alcanza: ahora hay un momento intermedio, entre que el pedido se guarda y que se sabe si la reserva de stock salió bien, y hace falta poder "confirmarlo" o "cancelarlo" según el resultado.

```java
// dominio/modelo/objetovalor/EstadoPedido.java
public enum EstadoPedido {
	PENDIENTE_CONFIRMACION,
	CONFIRMADO,
	CANCELADO
}
```

`Pedido.crear(...)` arranca en `PENDIENTE_CONFIRMACION`. Los dos métodos nuevos hacen la transición, y ambos son idempotentes frente a una llamada repetida:

```java
// dominio/modelo/agregado/Pedido.java
public void confirmar() {
	if (estado == EstadoPedido.CONFIRMADO) {
		return;
	}
	this.estado = EstadoPedido.CONFIRMADO;
}

public void cancelar(String motivo) {
	if (estado == EstadoPedido.CANCELADO) {
		return;
	}
	this.estado = EstadoPedido.CANCELADO;
	this.motivoCancelacion = motivo;
}
```

> **¿Por qué es exactamente la misma máquina de estados que el capítulo 13?**
>
> Porque la máquina de estados no sabe nada de cómo se llega a `confirmar()` o `cancelar(...)` — solo que alguien la llama. Que quien la invoque sea un `Consumer<T>` reaccionando a un evento (capítulo 13) o el propio `CrearPedidoServicio` en la misma llamada síncrona (este capítulo) es indiferente para `Pedido`: la Saga cambia cómo se decide, no qué significa "confirmado" o "cancelado" para un pedido. Por eso este agregado, `EstadoPedido` y su migración de esquema son la única pieza genuinamente idéntica entre las dos ramas.

### El comando: `ReservarStockComando` y el nuevo endpoint de `servicio-inventario`

`servicio-pedidos` construye el comando a partir del `Pedido` ya guardado:

```java
// dominio/comando/ReservarStockComando.java
public record ReservarStockComando(PedidoId pedidoId, List<LineaReserva> lineas) {

	public record LineaReserva(ProductoId productoId, Cantidad cantidad) {
	}
}
```

Y lo envía por HTTP síncrono, reutilizando el mismo patrón HTTP Service Client del capítulo 7 (`@HttpExchange`, `@ImportHttpServices`) que ya usa la llamada a `servicio-catalogo`:

```java
// infraestructura/adaptador/salida/http/cliente/InventarioHttpExchange.java
public interface InventarioHttpExchange {

	@PostExchange("/api/v1/reservas-stock")
	void reservarStock(@RequestBody ReservaStockPeticion peticion);
}
```

`servicio-inventario` no tenía, hasta ahora, ninguna API REST propia — solo escuchaba eventos. Este capítulo le añade su primer adaptador de entrada REST, sobre el mismo `ReservarStockPuertoEntrada`/`ReservarStockServicio` que ya existían desde el capítulo 12 (ninguno de los dos cambia: solo cambia quién los llama):

```java
// infraestructura/adaptador/entrada/rest/ReservaStockController.java
@PrimaryAdapter
@RestController
@RequestMapping("/api/v1/reservas-stock")
@RequiredArgsConstructor
public class ReservaStockController {

	private final ReservarStockPuertoEntrada reservarStockPuertoEntrada;

	@PostMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void reservar(@RequestBody ReservaStockPeticionDTO peticion) {
		reservarStockPuertoEntrada.reservar(peticion.pedidoId(), peticion.lineas());
	}
}
```

`StockInsuficienteException`/`StockNoEncontradoException` seguían sin capturar en ningún sitio hasta este capítulo — se perdían en el binder de Kafka (ver [motivación](#1-motivación-y-arquitectura-general-del-capítulo)). Ahora las captura un `ControladorErroresGlobal` nuevo, siguiendo el mismo patrón RFC 9457/`ProblemDetail` del capítulo 5. `StockInsuficienteException` responde `409 Conflict`; `StockNoEncontradoException` responde `404 Not Found`.

### La respuesta: `ResultadoReservaStock`

Del otro lado, `servicio-pedidos` necesita distinguir "la reserva salió bien" de "salió mal, y este es el motivo" — sin recurrir a una excepción para el segundo caso, porque un rechazo de stock es un desenlace de negocio esperado en una Saga, no un fallo técnico. Una **interfaz sellada** (Sealed Interface, Java 17) modela ambos desenlaces como tipos cerrados y exhaustivos:

```java
// dominio/modelo/objetovalor/ResultadoReservaStock.java
public sealed interface ResultadoReservaStock permits ReservaConfirmada, ReservaRechazada {
}

public record ReservaConfirmada() implements ResultadoReservaStock {
}

public record ReservaRechazada(String motivo) implements ResultadoReservaStock {
}
```

`InventarioAdaptador` traduce la respuesta HTTP a uno de los dos, leyendo el `detail` del `ProblemDetail` que devuelve `servicio-inventario` como motivo:

```java
// infraestructura/adaptador/salida/http/adaptador/InventarioAdaptador.java
@Override
public ResultadoReservaStock reservarStock(ReservarStockComando comando) {
	try {
		inventarioHttpExchange.reservarStock(aPeticion(comando));
		return new ReservaConfirmada();
	} catch (HttpClientErrorException.Conflict | HttpClientErrorException.NotFound excepcion) {
		return new ReservaRechazada(extraerMotivo(excepcion));
	}
}
```

> **¿Y si `servicio-inventario` no responde en absoluto (caída, timeout)?**
>
> Eso sigue siendo excepcional de verdad — no un desenlace de negocio esperado — y por tanto sigue propagándose como excepción (`ResourceAccessException`) en vez de como un tercer caso de `ResultadoReservaStock`. `ControladorErroresGlobal` de `servicio-pedidos` ya tenía un manejador para esa excepción desde el capítulo 8, pensado entonces solo para la llamada a catálogo; su mensaje ya no puede nombrar a un único servicio, porque ahora dos adaptadores distintos pueden lanzar el mismo tipo de excepción sin que Spring distinga cuál fue. Este capítulo, deliberadamente, no añade `@Retryable`/`@CircuitBreaker`/`@ConcurrencyLimit` a `InventarioAdaptador` — esas piezas de resiliencia ya se explicaron a fondo en el capítulo 8 sobre la llamada a catálogo, y repetirlas aquí no enseñaría nada nuevo; el pedido queda simplemente `PENDIENTE_CONFIRMACION` si la llamada falla, sin reintento automático, una limitación real que un capítulo futuro podría cerrar.

### El orquestador: `CrearPedidoServicio`

Con las tres piezas anteriores, `CrearPedidoServicio` — que hasta el capítulo 12 terminaba en cuanto guardaba el `Pedido` y publicaba su evento — pasa a ser el orquestador completo de la Saga. Ya no publica nada: llama, espera la respuesta, y decide:

```java
// aplicacion/servicio/CrearPedidoServicio.java
Pedido guardado = pedidoRepositorioPuertoSalida.guardar(pedido);

// El orquestador de la Saga: llama al siguiente paso explícitamente y, con la
// respuesta ya en la mano, decide si confirma o compensa cancelando el pedido.
ResultadoReservaStock resultado = inventarioPuertoSalida.reservarStock(aComando(guardado));
switch (resultado) {
	case ReservaConfirmada() -> guardado.confirmar();
	case ReservaRechazada rechazada -> guardado.cancelar(rechazada.motivo());
}

return pedidoMapper.aDTO(pedidoRepositorioPuertoSalida.guardar(guardado));
```

El `switch` sobre `resultado` es exhaustivo — el compilador conoce los dos únicos casos posibles de `ResultadoReservaStock` y no exige (ni permite) una rama `default`; si un capítulo futuro añadiera un tercer desenlace a la interfaz sellada, este `switch` dejaría de compilar hasta que se contemple explícitamente. Ese es el orquestador completo. No hay ninguna otra clase que conozca la secuencia "crear → reservar → confirmar o cancelar" — vive entera aquí, en un único método.

Con esto, `POST /api/pedidos` deja de devolver `201 Created` como una promesa a ciegas. Sigue devolviendo `201 Created` siempre — el recurso `Pedido` se crea igual, tenga stock o no —, pero ahora el cuerpo de esa misma respuesta ya lleva el desenlace real en `estado` (`CONFIRMADO`/`CANCELADO`) y, si aplica, `motivoCancelacion`. El cliente ya no necesita consultar nada después para saber qué pasó.

> **¿Por qué no devolver `409 Conflict` cuando la reserva se rechaza?**
>
> Porque el recurso `Pedido` sí se creó — tiene un id, es consultable, existe en la base de datos —; lo que no se confirmó fue la reserva de stock, y eso es exactamente lo que dice su `estado`. Modelar el resultado en el cuerpo, no en el código de estado HTTP, evita mezclar dos preguntas distintas ("¿se creó el recurso?" y "¿tuvo éxito el proceso de negocio que arrancó con esa creación?") en un único código de estado.

Lo que desaparece frente al capítulo 12 es todo lo que solo existía para sostener la publicación asíncrona del paso 2: el Outbox transaccional de `servicio-pedidos` (`OutboxPuertoSalida`, `OutboxPollerScheduler`, la tabla `outbox_evento`), `PedidoCreadoEvento`, y el consumidor `PedidoCreadoConsumidorConfiguracion` de `servicio-inventario` — sin él, seguiría reservando stock una segunda vez por cada pedido, por duplicado con la llamada HTTP nueva. Con eso se va también la dependencia de Kafka en `servicio-pedidos`: ya no publica nada, solo llama.

## 3. Cómo probarlo

![Diagrama de secuencia de la Saga orquestada a mano: desde POST /api/pedidos hasta CONFIRMADO o CANCELADO, con el comando y su respuesta viajando por HTTP síncrono entre servicio-pedidos y servicio-inventario, y una única respuesta al cliente al final de toda la secuencia](docs/images/capitulo-14/secuencia-saga.png)

*Secuencia completa de los pasos que siguen a continuación — el cliente recibe una sola respuesta, al final del todo, ya con el desenlace resuelto (confirmación arriba, compensación abajo).*
<br>

```bash
# Todos los tests del reactor (los tres microservicios)
./mvnw test
```

A diferencia del capítulo 13, este capítulo no toca el Kafka compartido de la raíz en ningún momento — el comando y su respuesta viajan por HTTP directo entre `servicio-pedidos` y `servicio-inventario`, no por un *broker*. Arrancar los tres microservicios ya trae consigo el resto de la infraestructura que hace falta, cada uno con su propio `compose.yaml` automático: Neo4j y RabbitMQ con `servicio-catalogo` (RabbitMQ sigue siendo necesario — `producto-creado` da de alta el stock, y esa parte no cambia desde el capítulo 12), PostgreSQL con `servicio-pedidos` y con `servicio-inventario`.

```bash
./mvnw -pl servicio-catalogo spring-boot:run &
```

```bash
./mvnw -pl servicio-pedidos spring-boot:run &
```

```bash
./mvnw -pl servicio-inventario spring-boot:run &
```

Categoría y producto en el catálogo — el alta del producto dispara, de fondo, la creación de su stock en `servicio-inventario` (100 unidades), igual que en el capítulo 12:

```bash
curl -X POST http://localhost:8080/api/categorias \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Deportes"}'
# => {"id":"<categoriaId>","nombre":"Deportes"}
```

```bash
curl -X POST http://localhost:8080/api/productos \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Balón","descripcion":"Balón de fútbol","precio":25.0,"categoriaId":"<categoriaId>"}'
# => {"id":"<productoId>","nombre":"Balón", ...}
```

**Camino feliz**: un pedido que el stock puede cubrir de sobra. A diferencia del capítulo 13, la propia respuesta ya lleva el desenlace — no hace falta esperar ni consultar nada aparte para saber qué pasó.

```bash
curl -X POST http://localhost:8081/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{"clienteId":"3fa85f64-5717-4562-b3fc-2c963f66afa6","lineas":[{"productoId":"<productoId>","cantidad":5}]}'
# => {"id":"<pedidoId>", ..., "estado":"CONFIRMADO", "motivoCancelacion":null} — 201 Created
```

```bash
docker exec -it servicio-inventario-postgres-1 psql -U inventario -d inventario \
  -c "SELECT * FROM stock WHERE producto_id = '<productoId>';"
# => cantidad = 95
```

**Camino de compensación**: un segundo pedido que pide más unidades de las que quedan (95). Otra vez, sin esperar nada.

```bash
curl -X POST http://localhost:8081/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{"clienteId":"3fa85f64-5717-4562-b3fc-2c963f66afa6","lineas":[{"productoId":"<productoId>","cantidad":150}]}'
# => {"id":"<otroPedidoId>", ..., "estado":"CANCELADO",
#     "motivoCancelacion":"Stock insuficiente para el producto <productoId>: disponible 95, solicitado 150"}
# — sigue 201 Created: el recurso Pedido se creó igual (ver la nota de la sección 2)
```

```bash
docker exec -it servicio-inventario-postgres-1 psql -U inventario -d inventario \
  -c "SELECT * FROM stock WHERE producto_id = '<productoId>';"
# => cantidad = 95 — sin cambios: la reserva rechazada no decrementa nada
```

Para parar los tres procesos (lanzados en segundo plano con `&`, sin ningún proceso en primer plano al que enviarle `Ctrl+C`) y su infraestructura, arrancada automáticamente junto a cada uno:

```bash
pkill -f "spring-boot:run"
```

## 4. Registro de archivos del capítulo

Leyenda: 🌱 Creado · ✏️ Actualizado · 🗑️ Eliminado.

### Build y configuración

|  | Archivo | Descripción funcional | Descripción del cambio |
|:---:|---|---|---|
| ✏️ | [`pom.xml`](servicio-pedidos/pom.xml) (servicio-pedidos) | Build Maven del módulo | Quita `spring-cloud-stream-binder-kafka`, sin uso una vez desaparece el Outbox |
| ✏️ | [`application.yml`](servicio-pedidos/src/main/resources/application.yml) (servicio-pedidos) | Configuración de Spring Boot del microservicio | Quita `spring.kafka`/`spring.cloud.stream`; añade `spring.http.serviceclient.inventario` |
| 🗑️ | `V2__crear_tabla_outbox.sql` (servicio-pedidos) | Migración Flyway: tabla `outbox_evento` | --- |
| 🌱 | [`V2__anadir_estado_pedido.sql`](servicio-pedidos/src/main/resources/db/migration/V2__anadir_estado_pedido.sql) (servicio-pedidos) | Migración Flyway: columnas `estado` y `motivo_cancelacion` en `pedidos` | --- |
| ✏️ | [`pom.xml`](servicio-inventario/pom.xml) (servicio-inventario) | Build Maven del módulo | Añade `spring-boot-starter-webmvc`(+`-test`) para el endpoint REST nuevo; quita `spring-cloud-stream-binder-kafka`, sin uso una vez desaparece el consumidor de `pedido-creado` |
| ✏️ | [`application.yml`](servicio-inventario/src/main/resources/application.yml) (servicio-inventario) | Configuración de Spring Boot del microservicio | Añade `server.port`; quita `spring.kafka` y el *binding* `pedidoCreadoConsumidor-in-0` |

### Dominio (servicio-pedidos)

|  | Archivo | Descripción funcional | Descripción del cambio |
|:---:|---|---|---|
| 🌱 | [`EstadoPedido.java`](servicio-pedidos/src/main/java/com/javacadabra/tienda/pedidos/dominio/modelo/objetovalor/EstadoPedido.java) | Objeto de Valor: estado del pedido (`PENDIENTE_CONFIRMACION`/`CONFIRMADO`/`CANCELADO`) | --- |
| ✏️ | [`Pedido.java`](servicio-pedidos/src/main/java/com/javacadabra/tienda/pedidos/dominio/modelo/agregado/Pedido.java) | Agregado raíz | Añade `estado`/`motivoCancelacion` y los métodos `confirmar()`/`cancelar(motivo)`, ambos idempotentes |
| 🗑️ | `PedidoCreadoEvento.java` | Evento de Dominio: pedido creado | --- |
| 🌱 | [`ReservarStockComando.java`](servicio-pedidos/src/main/java/com/javacadabra/tienda/pedidos/dominio/comando/ReservarStockComando.java) | Comando: instrucción para reservar el stock de un pedido | --- |
| 🌱 | [`ResultadoReservaStock.java`](servicio-pedidos/src/main/java/com/javacadabra/tienda/pedidos/dominio/modelo/objetovalor/ResultadoReservaStock.java) | Interfaz sellada del desenlace de una reserva de stock | --- |
| 🌱 | [`ReservaConfirmada.java`](servicio-pedidos/src/main/java/com/javacadabra/tienda/pedidos/dominio/modelo/objetovalor/ReservaConfirmada.java) | Objeto de Valor: la reserva de stock tuvo éxito | --- |
| 🌱 | [`ReservaRechazada.java`](servicio-pedidos/src/main/java/com/javacadabra/tienda/pedidos/dominio/modelo/objetovalor/ReservaRechazada.java) | Objeto de Valor: la reserva de stock se rechazó, con el motivo | --- |

### Dominio (servicio-inventario)

|  | Archivo | Descripción funcional | Descripción del cambio |
|:---:|---|---|---|
| ✏️ | [`StockInsuficienteException.java`](servicio-inventario/src/main/java/com/javacadabra/tienda/inventario/dominio/excepcion/StockInsuficienteException.java) | Excepción de dominio: la cantidad pedida supera el stock disponible | Expone `productoId` (`@Getter`), para que `ControladorErroresGlobal` lo incluya en el `ProblemDetail` |
| ✏️ | [`StockNoEncontradoException.java`](servicio-inventario/src/main/java/com/javacadabra/tienda/inventario/dominio/excepcion/StockNoEncontradoException.java) | Excepción de dominio: el producto no tiene fila de stock | Mismo cambio que la anterior |

### Aplicación (servicio-pedidos)

|  | Archivo | Descripción funcional | Descripción del cambio |
|:---:|---|---|---|
| 🗑️ | `OutboxPuertoSalida.java` | Puerto de salida: guardar un evento en la tabla outbox | --- |
| 🌱 | [`InventarioPuertoSalida.java`](servicio-pedidos/src/main/java/com/javacadabra/tienda/pedidos/aplicacion/puerto/salida/InventarioPuertoSalida.java) | Puerto de salida: enviar el comando de reserva de stock y obtener el resultado | --- |
| ✏️ | [`CrearPedidoServicio.java`](servicio-pedidos/src/main/java/com/javacadabra/tienda/pedidos/aplicacion/servicio/CrearPedidoServicio.java) | Servicio de aplicación, caso de uso crear un pedido | Pasa a ser el orquestador de la Saga: tras guardar el pedido, llama a `InventarioPuertoSalida` y confirma o cancela según la respuesta |
| ✏️ | [`PedidoDTO.java`](servicio-pedidos/src/main/java/com/javacadabra/tienda/pedidos/aplicacion/dto/salida/PedidoDTO.java) | DTO de salida de `Pedido` | Añade `estado` y `motivoCancelacion` |
| ✏️ | [`PedidoMapper.java`](servicio-pedidos/src/main/java/com/javacadabra/tienda/pedidos/aplicacion/mapper/PedidoMapper.java) | Mapper MapStruct dominio→DTO de `Pedido` | Mapea `estado`/`motivoCancelacion` |

### Aplicación (servicio-inventario)

|  | Archivo | Descripción funcional | Descripción del cambio |
|:---:|---|---|---|
| 🌱 | [`ReservaStockPeticionDTO.java`](servicio-inventario/src/main/java/com/javacadabra/tienda/inventario/aplicacion/dto/entrada/ReservaStockPeticionDTO.java) | DTO de entrada: cuerpo JSON del comando de reserva de stock | --- |

### Infraestructura de entrada (servicio-pedidos)

|  | Archivo | Descripción funcional | Descripción del cambio |
|:---:|---|---|---|
| ✏️ | [`ControladorErroresGlobal.java`](servicio-pedidos/src/main/java/com/javacadabra/tienda/pedidos/infraestructura/adaptador/entrada/rest/ControladorErroresGlobal.java) | `@RestControllerAdvice`, traduce excepciones a `ProblemDetail` | Generaliza el manejador de `ResourceAccessException` (ya no puede nombrar solo a catálogo, ahora también lo lanza la llamada a inventario) |

### Infraestructura de entrada (servicio-inventario)

|  | Archivo | Descripción funcional | Descripción del cambio |
|:---:|---|---|---|
| 🗑️ | `PedidoCreadoConsumidorConfiguracion.java` | Consumidor de `pedido-creado` sobre Kafka | --- |
| 🗑️ | `PedidoCreadoEventoDTO.java` | Traducción propia (Capa Anticorrupción) del `PedidoCreadoEvento` ajeno | --- |
| 🌱 | [`ReservaStockController.java`](servicio-inventario/src/main/java/com/javacadabra/tienda/inventario/infraestructura/adaptador/entrada/rest/ReservaStockController.java) | Primer adaptador de entrada REST de `servicio-inventario`: `POST /api/v1/reservas-stock` | --- |
| 🌱 | [`ControladorErroresGlobal.java`](servicio-inventario/src/main/java/com/javacadabra/tienda/inventario/infraestructura/adaptador/entrada/rest/ControladorErroresGlobal.java) | `@RestControllerAdvice`, traduce `StockInsuficienteException`/`StockNoEncontradoException` a `ProblemDetail` (409/404) | --- |

### Infraestructura de salida (servicio-pedidos)

|  | Archivo | Descripción funcional | Descripción del cambio |
|:---:|---|---|---|
| 🗑️ | `OutboxPollerScheduler.java` | Sondea la tabla outbox y publica en Kafka | --- |
| 🗑️ | `OutboxRepositorioAdaptador.java` | Adaptador de salida: serializa el evento a JSON y lo guarda en la tabla outbox | --- |
| 🗑️ | `OutboxEventoEntidad.java` | Entidad JPA de la tabla `outbox_evento` | --- |
| 🗑️ | `OutboxEventoRepositorioJpa.java` | Repositorio Spring Data de `outbox_evento` | --- |
| ✏️ | [`PedidoEntidad.java`](servicio-pedidos/src/main/java/com/javacadabra/tienda/pedidos/infraestructura/adaptador/salida/persistencia/entidad/PedidoEntidad.java) | Entidad JPA de la tabla `pedidos` | Añade las columnas `estado` y `motivoCancelacion` |
| ✏️ | [`PedidoEntidadMapper.java`](servicio-pedidos/src/main/java/com/javacadabra/tienda/pedidos/infraestructura/adaptador/salida/persistencia/mapper/PedidoEntidadMapper.java) | Mapper MapStruct dominio↔entidad de `Pedido` | Mapea `estado`/`motivoCancelacion` en ambas direcciones |
| 🌱 | [`InventarioHttpExchange.java`](servicio-pedidos/src/main/java/com/javacadabra/tienda/pedidos/infraestructura/adaptador/salida/http/cliente/InventarioHttpExchange.java) | HTTP Service Client hacia `servicio-inventario` | --- |
| 🌱 | [`ReservaStockPeticion.java`](servicio-pedidos/src/main/java/com/javacadabra/tienda/pedidos/infraestructura/adaptador/salida/http/cliente/ReservaStockPeticion.java) | Cuerpo JSON de la petición HTTP a `servicio-inventario` | --- |
| 🌱 | [`InventarioAdaptador.java`](servicio-pedidos/src/main/java/com/javacadabra/tienda/pedidos/infraestructura/adaptador/salida/http/adaptador/InventarioAdaptador.java) | Adaptador de salida: llama a `servicio-inventario` y traduce la respuesta a `ResultadoReservaStock` | --- |
| ✏️ | [`ClientesHttpConfig.java`](servicio-pedidos/src/main/java/com/javacadabra/tienda/pedidos/infraestructura/configuracion/ClientesHttpConfig.java) | Registro de los HTTP Service Clients del microservicio | Añade `@ImportHttpServices(group = "inventario", ...)` |

### Documentación/diagramas

|  | Archivo | Descripción funcional | Descripción del cambio |
|:---:|---|---|---|
| 🌱 | [`capitulo-14-secuencia-saga.excalidraw`](docs/diagramas/capitulo-14-secuencia-saga.excalidraw) | Diagrama de secuencia de la Saga orquestada a mano, con los dos caminos (confirmación/compensación) | --- |
| 🌱 | [`secuencia-saga.png`](docs/images/capitulo-14/secuencia-saga.png) | Render PNG del diagrama anterior | --- |

### Tests

|  | Archivo | Descripción funcional | Descripción del cambio |
|:---:|---|---|---|
| ✏️ | [`CrearPedidoServicioTest.java`](servicio-pedidos/src/test/java/com/javacadabra/tienda/pedidos/aplicacion/servicio/CrearPedidoServicioTest.java) | Test unitario de `CrearPedidoServicio` | Sustituye el mock de `OutboxPuertoSalida` por `InventarioPuertoSalida`; añade el caso de reserva rechazada |
| ✏️ | [`PedidoTest.java`](servicio-pedidos/src/test/java/com/javacadabra/tienda/pedidos/dominio/modelo/agregado/PedidoTest.java) | Test unitario del agregado `Pedido` | Añade casos de `confirmar()`/`cancelar(motivo)`, incluida la idempotencia del motivo de cancelación |
| ✏️ | [`PedidoControllerRestTestClientIntegrationTest.java`](servicio-pedidos/src/test/java/com/javacadabra/tienda/pedidos/infraestructura/adaptador/entrada/rest/PedidoControllerRestTestClientIntegrationTest.java) | Test extremo a extremo del controlador de pedidos | Añade el `MockRestServiceServer` del grupo `inventario`; verifica `estado` en la respuesta |
| ✏️ | [`CatalogoAdaptadorResilienciaTest.java`](servicio-pedidos/src/test/java/com/javacadabra/tienda/pedidos/infraestructura/adaptador/salida/http/adaptador/CatalogoAdaptadorResilienciaTest.java) | Test del *circuit breaker* hacia catálogo | Quita la propiedad `outbox.poller.enabled`, sin efecto ya |
| 🌱 | [`InventarioAdaptadorIntegrationTest.java`](servicio-pedidos/src/test/java/com/javacadabra/tienda/pedidos/infraestructura/adaptador/salida/http/adaptador/InventarioAdaptadorIntegrationTest.java) | Test de `InventarioAdaptador` con `MockRestServiceServer` | --- |
| 🌱 | [`ReservaStockControllerTest.java`](servicio-inventario/src/test/java/com/javacadabra/tienda/inventario/infraestructura/adaptador/entrada/rest/ReservaStockControllerTest.java) | Test de `ReservaStockController` (`@WebMvcTest`) | --- |

## 5. Referencias

- [microservices.io — Pattern: Saga](https://microservices.io/patterns/data/saga.html)
- [microservices.io — A tour of two sagas: choreography vs. orchestration](https://microservices.io/post/architecture/2024/03/20/tour-of-two-sagas.html)
- [HTTP Interface Client (Spring Framework Reference)](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#rest-http-interface)
- [HTTP Service Clients (Spring Boot Reference)](https://docs.spring.io/spring-boot/reference/io/rest-client.html)
- [RFC 9457 — Problem Details for HTTP APIs](https://www.rfc-editor.org/rfc/rfc9457)
- [JEP 409 — Sealed Classes](https://openjdk.org/jeps/409)
