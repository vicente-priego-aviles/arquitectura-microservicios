# Capítulo 09 — Test de la capa web

Noveno capítulo del tutorial "De cero a pro en arquitectura de microservicios con Spring Boot" (ver el índice completo de capítulos en la rama `main`). Parte directamente de `capitulo-08-resiliencia`.

## Índice

1. [Introducción](#1-introducción)
2. [`@WebMvcTest` y `MockMvcTester`: el Test Slice web](#2-webmvctest-y-mockmvctester-el-test-slice-web)
3. [`RestTestClient` sobre el mismo Test Slice: cuerpo tipado](#3-resttestclient-sobre-el-mismo-test-slice-cuerpo-tipado)
4. [Las cuatro formas de vincular `RestTestClient`](#4-las-cuatro-formas-de-vincular-resttestclient)
   1. [El extremo más rápido: `bindToController`](#41-el-extremo-más-rápido-bindtocontroller)
   2. [El extremo a extremo: servidor HTTP real](#42-el-extremo-a-extremo-servidor-http-real)
5. [Cómo probarlo](#5-cómo-probarlo)
6. [Registro de archivos del capítulo](#6-registro-de-archivos-del-capítulo)
7. [Referencias](#7-referencias)

---

## 1. Introducción

Ningún controlador REST del proyecto tiene test propio desde el capítulo 1: `ProductoController`/`CategoriaController` de `servicio-catalogo` y `PedidoController` de `servicio-pedidos` solo se han verificado a mano, vía Swagger UI o `curl`. Funciona para explorar mientras se escribe el código, pero no deja nada que vuelva a ejecutarse solo — ni protege de una regresión al tocar un `@ExceptionHandler`, ni documenta de forma ejecutable el contrato HTTP real de cada endpoint (código de estado, cabeceras, forma del cuerpo).

Spring Boot 4 ofrece dos APIs para cerrar ese hueco, ambas construidas sobre la misma infraestructura de `MockMvc` por debajo — de hecho, una de las dos formas de construir `RestTestClient` envuelve directamente una instancia de `MockMvc` ya configurada — pero con propósitos distintos:

| API | Punto fuerte | Cuándo elegirla |
|---|---|---|
| `MockMvcTester` | Inspección del lado del servidor: qué *handler* se invocó, qué excepción se lanzó; única con soporte de subida de múltiples ficheros | Verificar cómo reacciona el servidor por dentro, no solo la respuesta que ve el cliente |
| `RestTestClient` | Una sola API que sirve igual para tests mockeados que para un servidor real; deserialización tipada del cuerpo de la respuesta en vez de navegar la respuesta *JSON path* a *JSON path* | Verificar el contrato HTTP tal y como lo vería un cliente real, con el cuerpo ya convertido al DTO de salida |

Este capítulo demuestra ambas sobre los mismos endpoints de `ProductoController` para que se puedan comparar directamente, y luego dedica una sección aparte a las cuatro formas de vincular `RestTestClient` a un servidor — desde la más aislada (sin contexto de Spring) hasta la más realista (servidor HTTP real de punta a punta).

De paso, con dos microservicios ya repitiendo el mismo patrón de anotaciones Swagger en sus controllers, aprovechamos para extraerlas a una interfaz propia que el controller se limita a implementar — una limpieza pequeña, pero que vale la pena hacer ahora que el patrón se repite por segunda vez.

## 2. `@WebMvcTest` y `MockMvcTester`: el Test Slice web

`@WebMvcTest(ProductoController.class)` carga solo el **Test Slice** relacionado con ese controlador — un subconjunto del contexto de Spring limitado a la infraestructura MVC (`DispatcherServlet`, conversores de mensaje, `@RestControllerAdvice` como `ControladorErroresGlobal`), sin arrancar Neo4j ni escanear servicios o repositorios. Los cinco puertos de entrada que usa `ProductoController` se sustituyen por dobles de Mockito con `@MockitoBean`, y Spring Boot autoconfigura un `MockMvcTester` listo para inyectar porque AssertJ ya está en el classpath de test:

```java
// servicio-catalogo/src/test/java/.../infraestructura/adaptador/entrada/rest/ProductoControllerTest.java
@WebMvcTest(ProductoController.class)
@AutoConfigureRestTestClient
class ProductoControllerTest {

	@Autowired
	private MockMvcTester mvc;

	@MockitoBean
	private CrearProductoPuertoEntrada crearProductoPuertoEntrada;
	// ... un @MockitoBean por cada puerto de entrada que declara el controller

	@Test
	void crearUnProductoDevuelve201YElCuerpoConMockMvcTester() {
		String categoriaId = UUID.randomUUID().toString();
		ProductoDTO creado = new ProductoDTO(UUID.randomUUID().toString(), "Camiseta", "Ropa de algodón", new BigDecimal("19.99"), categoriaId);
		when(crearProductoPuertoEntrada.crear(any())).thenReturn(creado);

		assertThat(mvc.post().uri("/api/productos")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"nombre":"Camiseta","descripcion":"Ropa de algodón","precio":19.99,"categoriaId":"%s"}
						""".formatted(categoriaId)))
				.hasStatus(HttpStatus.CREATED)
				.bodyJson()
				.extractingPath("$.nombre").isEqualTo("Camiseta");
	}
}
```

`assertThat(mvc.post()...)` ejecuta la petición y devuelve un resultado sobre el que encadenar aserciones AssertJ (`hasStatus`, `bodyJson().extractingPath(...)`) — sin volcar JSON crudo ni comparar cadenas completas.

El punto fuerte real de `MockMvcTester` aparece al verificar cómo reacciona el servidor ante un fallo, no solo qué código de estado devuelve. `ControladorErroresGlobal` traduce `ProductoNoEncontradoException` a un `ProblemDetail` con `404`; el test lo comprueba disparando esa excepción directamente desde el doble del puerto de entrada, sin necesidad de un producto real que no exista:

```java
@Test
void buscarUnProductoInexistenteDevuelve404ConProblemDetailConMockMvcTester() {
	String id = UUID.randomUUID().toString();
	when(buscarProductoPuertoEntrada.buscarPorId(id)).thenThrow(new ProductoNoEncontradoException(id));

	assertThat(mvc.get().uri("/api/productos/{id}", id))
			.hasStatus(HttpStatus.NOT_FOUND)
			.bodyJson()
			.extractingPath("$.title").isEqualTo("Producto no encontrado");
}
```

---

## 3. `RestTestClient` sobre el mismo Test Slice: cuerpo tipado

`@AutoConfigureRestTestClient` en la misma clase `@WebMvcTest` inyecta también un `RestTestClient` que reutiliza el `MockMvc` ya configurado — mismo endpoint, misma petición, pero con una API pensada para consumir la respuesta como el objeto de salida real en vez de navegar el JSON:

```java
@Test
void buscarUnProductoExistenteDevuelveSuCuerpoTipadoConRestTestClient() {
	String id = UUID.randomUUID().toString();
	ProductoDTO producto = new ProductoDTO(id, "Teclado", "Switches azules", new BigDecimal("49.99"), UUID.randomUUID().toString());
	when(buscarProductoPuertoEntrada.buscarPorId(id)).thenReturn(producto);

	restTestClient.get().uri("/api/productos/{id}", id)
			.exchange()
			.expectStatus().isOk()
			.expectBody(ProductoDTO.class)
			.value(cuerpo -> assertThat(cuerpo.nombre()).isEqualTo("Teclado"));
}
```

`expectBody(ProductoDTO.class)` deserializa la respuesta directamente al DTO real que usa `ProductoController`, y `.value(...)` recibe ya ese objeto tipado para aserciones AssertJ normales — comparado con `mvc.get()...bodyJson().extractingPath("$.nombre")` de la sección anterior, aquí no hace falta conocer la forma del JSON, solo el tipo Java de salida. La diferencia se nota más con una lista: `expectBody(new ParameterizedTypeReference<List<ProductoDTO>>() {})` deserializa `GET /api/productos?categoriaId=...` directamente a `List<ProductoDTO>`, sin extraer cada campo con *JSON path*.

---

## 4. Las cuatro formas de vincular `RestTestClient`

`RestTestClient` no está atado a `@WebMvcTest`: se puede vincular a un controlador de cuatro formas distintas, con la misma API de ahí en adelante en los cuatro casos, y de menor a mayor aislamiento roto:

| Forma de vincularse | Contexto de Spring | Servidor HTTP real | Uso típico |
|---|---|---|---|
| `bindToController(...)` | Ninguno | No | Test unitario puro de un controller, dependencias mockeadas a mano |
| `bindTo(mockMvc)` / `@AutoConfigureRestTestClient` bajo `@WebMvcTest` | Test Slice web | No | El mismo Test Slice de la sección anterior |
| `bindToApplicationContext(context)` | Contexto completo | No | Dependencias reales, sin levantar un puerto |
| `bindToServer()` / `@AutoConfigureRestTestClient` con `@SpringBootTest(webEnvironment = RANDOM_PORT)` | Contexto completo | Sí | El único que verifica HTTP de verdad, cabeceras incluidas |

![Espectro de las cuatro formas de vincular RestTestClient, desde bindToController (sin contexto de Spring) hasta bindToServer (servidor HTTP real), con una barra de progreso por caja indicando cuánta infraestructura real interviene y qué clase de test de este capítulo demuestra cada una](docs/images/capitulo-09/resttestclient-formas-vinculacion.png)

*Las cuatro formas de vincular `RestTestClient`, de menor a mayor aislamiento roto — tres de las cuatro se demuestran en código en este capítulo; `bindToApplicationContext` queda solo documentado en la tabla anterior.*
<br>

Vale la pena mostrar al menos los dos extremos para entender la progresión completa, no solo el Test Slice web ya visto.

### 4.1 El extremo más rápido: `bindToController`

`bindToController(...)` arranca una **Configuración autónoma** (Standalone Setup): ni contexto de Spring ni `@WebMvcTest`, el controller se instancia a mano con dobles de Mockito normales (`@Mock`, no `@MockitoBean`). Es el test más rápido de los cuatro, pero el precio es que nada se descubre por escaneo — ni siquiera `ControladorErroresGlobal` se aplica automáticamente: hay que registrarlo explícitamente.

> **¿`.controllerAdvice(...)` no compila?**
>
> La forma intuitiva sería encadenar `RestTestClient.bindToController(controller).controllerAdvice(...)`, pero ese método no existe en `RestTestClient.StandaloneSetupBuilder`. El registro de un `@ControllerAdvice` en este modo se hace a través de `configureServer(...)`, que expone el `StandaloneMockMvcBuilder` de Spring MVC Test por debajo — la misma clase que ya tenía `setControllerAdvice(...)` antes de que existiera `RestTestClient`:
>
> ```java
> RestTestClient.bindToController(controller)
>         .configureServer(builder -> builder.setControllerAdvice(new ControladorErroresGlobal()))
>         .build();
> ```

```java
// servicio-catalogo/src/test/java/.../infraestructura/adaptador/entrada/rest/CategoriaControllerTest.java
@ExtendWith(MockitoExtension.class)
class CategoriaControllerTest {

	@Mock
	private CrearCategoriaPuertoEntrada crearCategoriaPuertoEntrada;

	@Mock
	private BuscarCategoriaPuertoEntrada buscarCategoriaPuertoEntrada;

	private RestTestClient restTestClient;

	@BeforeEach
	void construirClienteSinContextoDeSpring() {
		CategoriaController controller = new CategoriaController(crearCategoriaPuertoEntrada, buscarCategoriaPuertoEntrada);
		restTestClient = RestTestClient.bindToController(controller)
				.configureServer(builder -> builder.setControllerAdvice(new ControladorErroresGlobal()))
				.build();
	}

	@Test
	void buscarUnaCategoriaInexistenteDevuelve404ConProblemDetail() {
		String id = UUID.randomUUID().toString();
		when(buscarCategoriaPuertoEntrada.buscarPorId(id)).thenThrow(new CategoriaNoEncontradaException(id));

		restTestClient.get().uri("/api/categorias/{id}", id)
				.exchange()
				.expectStatus().isNotFound()
				.expectBody(ProblemDetail.class)
				.value(cuerpo -> assertThat(cuerpo.getTitle()).isEqualTo("Categoría no encontrada"));
	}
}
```

### 4.2 El extremo a extremo: servidor HTTP real

En el otro extremo, `@SpringBootTest(webEnvironment = RANDOM_PORT)` junto con `@AutoConfigureRestTestClient` arranca la aplicación completa en un puerto real y conecta el `RestTestClient` a ese servidor — el único de los cuatro que pasa de verdad por un socket HTTP, con serialización y cabeceras reales en vez de simuladas.

Para `PedidoController` eso implica también el resto de infraestructura real: PostgreSQL vía Testcontainers (igual que el test de resiliencia del capítulo 8) y la llamada saliente a `servicio-catalogo` interceptada con `MockRestServiceServer` sobre el grupo `catalogo` — la misma mecánica que ya fijaba el comportamiento de `@Retryable`/circuit breaker en el capítulo anterior, aquí reutilizada para dar una respuesta determinista sin depender de que `servicio-catalogo` esté levantado:

```java
// servicio-pedidos/src/test/java/.../infraestructura/adaptador/entrada/rest/PedidoControllerRestTestClientIntegrationTest.java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@Testcontainers
class PedidoControllerRestTestClientIntegrationTest {

	@Container
	@ServiceConnection
	static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest");

	@Autowired
	private RestTestClient restTestClient;

	// ConfiguracionMockCatalogo intercepta el grupo "catalogo" con MockRestServiceServer,
	// igual que CatalogoAdaptadorResilienciaTest en el capítulo 8

	@Test
	void crearUnPedidoDeExtremoAExtremoConServidorHttpRealYCabecerasVerificadas() {
		String productoId = UUID.randomUUID().toString();
		mockServer.expect(requestTo("http://localhost:8080/api/productos/" + productoId))
				.andRespond(withSuccess("""
						{"id":"%s","nombre":"Teclado","descripcion":"Switches azules","precio":49.99,"categoriaId":"%s"}
						""".formatted(productoId, UUID.randomUUID()), MediaType.APPLICATION_JSON));

		CrearPedidoDTO dto = new CrearPedidoDTO(UUID.randomUUID().toString(), List.of(new LineaPedidoDTO(productoId, 2)));

		restTestClient.post().uri("/api/pedidos")
				.contentType(MediaType.APPLICATION_JSON)
				.body(dto)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody(PedidoDTO.class)
				.value(pedido -> assertThat(pedido.total()).isEqualByComparingTo("99.98"));

		mockServer.verify();
	}
}
```

`expectHeader().contentType(...)` es la aserción que no tiene equivalente real en los otros tres modos: solo aquí la cabecera `Content-Type` la ha escrito de verdad el servidor Servlet al serializar la respuesta, no un objeto `MockHttpServletResponse` simulado.

---

## 5. Cómo probarlo

```bash
# Todos los tests del capítulo (ambos módulos)
./mvnw clean test

# Solo el Test Slice web de servicio-catalogo (rápidos, sin Docker)
./mvnw -pl servicio-catalogo test -Dtest=ProductoControllerTest,CategoriaControllerTest

# El test de extremo a extremo de servicio-pedidos (requiere Docker para Testcontainers/PostgreSQL)
./mvnw -pl servicio-pedidos test -Dtest=PedidoControllerRestTestClientIntegrationTest
```

`ProductoControllerTest` y `CategoriaControllerTest` no necesitan Docker ni Neo4j — son los dos extremos rápidos, `@WebMvcTest` y `bindToController`. `PedidoControllerRestTestClientIntegrationTest` sí, porque arranca la aplicación completa sobre un `PostgreSQLContainer` real.

---

## 6. Registro de archivos del capítulo

🌱 Creado · ✏️ Actualizado · 🗑️ Eliminado

### Tests

|     | Archivo | Descripción funcional | Descripción del cambio |
|:---:|---|---|---|
| 🌱 | [`servicio-catalogo/.../rest/ProductoControllerTest.java`](servicio-catalogo/src/test/java/com/javacadabra/tienda/catalogo/infraestructura/adaptador/entrada/rest/ProductoControllerTest.java) | Test Slice web (`@WebMvcTest`) de `ProductoController`, con `MockMvcTester` y `RestTestClient` sobre los mismos endpoints | --- |
| 🌱 | [`servicio-catalogo/.../rest/CategoriaControllerTest.java`](servicio-catalogo/src/test/java/com/javacadabra/tienda/catalogo/infraestructura/adaptador/entrada/rest/CategoriaControllerTest.java) | `RestTestClient.bindToController(...)` sobre `CategoriaController`: configuración autónoma sin contexto de Spring | --- |
| 🌱 | [`servicio-pedidos/.../rest/PedidoControllerRestTestClientIntegrationTest.java`](servicio-pedidos/src/test/java/com/javacadabra/tienda/pedidos/infraestructura/adaptador/entrada/rest/PedidoControllerRestTestClientIntegrationTest.java) | `RestTestClient` de extremo a extremo sobre `PedidoController`, servidor HTTP real + PostgreSQL vía Testcontainers + `MockRestServiceServer` para el catálogo | --- |

### Documentación

|     | Archivo | Descripción funcional | Descripción del cambio |
|:---:|---|---|---|
| 🌱 | [`docs/diagramas/capitulo-09-resttestclient-formas-vinculacion.excalidraw`](docs/diagramas/capitulo-09-resttestclient-formas-vinculacion.excalidraw) | Diagrama Excalidraw (fuente editable) del espectro de las cuatro formas de vincular `RestTestClient` | --- |
| 🌱 | [`docs/images/capitulo-09/resttestclient-formas-vinculacion.png`](docs/images/capitulo-09/resttestclient-formas-vinculacion.png) | Render del diagrama anterior, embebido en la [sección 4](#4-las-cuatro-formas-de-vincular-resttestclient) | --- |
| ✏️ | [`GLOSARIO.md`](GLOSARIO.md) | Traducciones fijadas de términos técnicos del tutorial | Añadidas las filas "Test Slice" y "Configuración autónoma" (Standalone Setup) |

## 7. Referencias

- [Testing Spring Boot Applications — Spring Boot Reference Documentation](https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html)
- [`RestTestClient` — Spring Framework API](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/test/web/servlet/client/RestTestClient.html)
- [`MockMvcTester` — Spring Framework API](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/test/web/servlet/assertj/MockMvcTester.html)
