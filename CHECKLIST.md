# Checklist tecnológico

Estado de las tecnologías/temas cubiertos en el tutorial, capítulo a capítulo. Vive en la rama del último capítulo (no en `main`) y se actualiza en cada capítulo nuevo.

## Hoja de ruta de próximos capítulos (tentativa)

Orden recomendado para los capítulos 6 en adelante, sujeto a revisión al planificar cada uno (como el resto del tutorial, el alcance final de cada capítulo se decide en conversación, no de antemano).

1. **Segundo microservicio: Carrito/Pedidos** — persistencia políglota (Spring Data JPA/PostgreSQL, para contrastar con el grafo de Neo4j) y llamada síncrona a `servicio-catalogo` vía HTTP Service Client de Spring (`@HttpExchange`/`@ImportHttpServices`, no OpenFeign — ver "Comunicación entre servicios" más abajo). Con dos microservicios repitiendo el mismo patrón, revisar la verbosidad de anotaciones Swagger en los controllers de `servicio-catalogo` (capítulo 3): extraer `@Operation`/`@ApiResponses` a una interfaz (`ProductoControllerApi`, etc., en el mismo paquete `infraestructura.adaptador.entrada.rest`) que el `@RestController` implemente — springdoc-openapi soporta anotaciones declaradas en interfaces (FAQ oficial) y Spring MVC resuelve igual las de `@RequestMapping`, dejando la clase controller con solo `@Override` y el cuerpo del método. **Resiliencia de esa llamada síncrona**: `@Retryable`/`@ConcurrencyLimit` nativos de Spring Framework 7 (`@EnableResilientMethods`) para reintentos — sustituyen a Spring Retry. Para circuit breaker real (máquina de estados open/half-open/closed), Spring Framework 7 no lo cubre nativamente: usar Resilience4j directamente, no Spring Cloud Circuit Breaker (el propio proyecto se declara superseded por Spring Framework 7 y sin más desarrollo — mismo caso que OpenFeign). **Migraciones de esquema**: con JPA/PostgreSQL entra por primera vez un esquema relacional versionado en el monorepo — Flyway o Liquibase para las migraciones, en vez de dejar que `ddl-auto` de Hibernate genere el esquema fuera del entorno de desarrollo.
2. **Disciplina de tests y arquitectura: ArchUnit + jMolecules + Instancio + DataFaker** — se pospone hasta tener dos microservicios porque el valor de automatizar la disciplina hexagonal (ArchUnit sobre anotaciones jMolecules, vía `jmolecules-archunit`) y de reducir boilerplate de fixtures (Instancio/DataFaker) crece con el número de servicios/agregados a mantener consistentes; con uno solo no compensa el coste de introducirlas.
3. **Mensajería asíncrona (Spring Cloud Stream, binders Kafka y RabbitMQ)** — los eventos de dominio del capítulo 4 salen del proceso; los consume el microservicio del capítulo 6. Spring Cloud Stream aporta la abstracción del binder (`Supplier`/`Function`/`Consumer` como `@Bean`, sin código específico del broker) — se demuestra su valor real implementando el mismo productor/consumidor sobre los dos binders (Kafka y RabbitMQ) y comprobando que solo cambia configuración, no código de negocio. Compatible con Spring Boot 4.1 desde Spring Cloud 2025.1.2 ("Oakwood", junio 2026). **Outbox transaccional**: publicar en Kafka/RabbitMQ ya no es atómico con el commit en base de datos como lo era `ApplicationEventPublisher` síncrono del capítulo 4 — guardar el evento en la misma transacción que el cambio de estado (tabla outbox) y publicarlo aparte (poller o CDC) evita el problema de doble escritura. **Consumidores idempotentes**: un mensaje puede entregarse más de una vez (redelivery); los listeners deben poder procesar el mismo evento dos veces sin duplicar el efecto (p. ej. clave de deduplicación por id de evento).
4. **Patrón Saga + compensación** — con Pedidos+Catálogo (y quizá Pagos) compartiendo una transacción de negocio. Los pasos de compensación también deben ser idempotentes (mismo motivo que los consumidores del punto 3): un paso de compensación reintentado no debe deshacer dos veces el mismo efecto.
5. **Observabilidad — Actuator + Micrometer** — instrumentar el servicio: exponer `/actuator/health`/`/actuator/metrics` y métricas propias (`@Timed`, `MeterRegistry`) sobre los casos de uso ya existentes. Micrometer es la fachada que Actuator usa por debajo; separarlo de Prometheus/Grafana dejaría claro qué aporta cada capa antes de meter infraestructura nueva en `compose.yaml`.
6. **Observabilidad — Prometheus + Grafana** — recolectar y visualizar: exponer `/actuator/prometheus`, levantar Prometheus y Grafana en `compose.yaml`, configurar el scraping y montar un dashboard real sobre las métricas del capítulo anterior. Con tráfico real entre servicios y mensajería de por medio (capítulos 6-8), hay algo real que observar.
7. **Observabilidad — Trazas distribuidas y logs centralizados** (Micrometer Tracing, bridge OTel + exporter OTLP → Grafana Tempo, más Loki para logs) — el tercer pilar de observabilidad junto a las métricas de los dos capítulos anteriores: con dos microservicios llamándose por HTTP y por mensajería asíncrona, una traza distribuida es la única forma de seguir una petición de punta a punta a través de ambos. Micrometer Tracing es la fachada (sucesora de Spring Cloud Sleuth); por debajo se elige un bridge — aquí OTel, con Spring Boot ya ofreciendo un starter dedicado para la combinación bridge OTel + exporter OTLP. Grafana ya está instalado desde el capítulo anterior, así que Tempo (trazas) y Loki (logs) se añaden como datasources del mismo Grafana, sin duplicar infraestructura de visualización. El README de ese capítulo debe incluir una nota explicando Zipkin como alternativa (backend de trazas autocontenido, con su propia UI, sin integración con Grafana), aclarando que esa combinación va con el bridge Brave, no con el bridge OTel — Spring Boot 4.2 elimina el soporte de OpenTelemetry-con-Zipkin (ya deprecado) — y por qué se elige Tempo en su lugar (evita fragmentar el "un solo Grafana para todo" que ya se viene construyendo desde el capítulo anterior).
8. **Seguridad: OAuth2/Keycloak/JWT** — proteger los endpoints de ambos microservicios antes de exponer nada a un frontend real o a un gateway público.
9. **API Gateway/BFF** (Spring Cloud Gateway Server MVC — variante no-reactiva, coherente con el resto del stack del proyecto) — punto de entrada único. **Autenticación centralizada de verdad, no solo mencionada**: el Gateway actúa como Cliente OAuth2 (login del capítulo anterior) y usa el filtro `TokenRelay` para reenviar el token del usuario autenticado a Catálogo/Pedidos en cada petición proxied — así cada microservicio sigue validando el token como Resource Server (capítulo 13), pero ninguno gestiona su propio flujo de login; eso es lo que centraliza el Gateway, no solo repetir la palabra. **BFF de verdad, no solo proxy**: al menos un endpoint propio del Gateway que agregue datos de Catálogo + Pedidos en una sola respuesta adaptada al frontend Vaadin (p. ej. un "resumen de pedido" que combine el pedido con los datos de producto de cada línea) — así el frontend no hace varias llamadas a servicios distintos y las ensambla él mismo; eso es lo que distingue un BFF de un simple reverse proxy que reenvía peticiones tal cual. **`RequestRateLimiter`**: filtro nativo de Spring Cloud Gateway (backed por Redis) para limitar peticiones por cliente — coste casi cero de añadir ya que el Gateway está montado, y es lo primero que se espera de un punto de entrada único expuesto de verdad. Preguntar entonces si conviene configurar el desplegable "Servers" de Swagger UI (capítulo 3) con `@Server` en `@OpenAPIDefinition` — mostrar la URL del Gateway en vez de (o además de) la de cada microservicio directamente, y/o un `ServerBaseUrlCustomizer` si hay proxy inverso de por medio. Con el Gateway agregando varios servicios bajo un único punto de entrada, vale la pena decidir también una estrategia mínima de versionado de API (URI vs. cabecera) — nota breve, no un capítulo propio.
10. **Frontend: Vaadin** — consume el Gateway, con login real y el flujo de negocio completo (catálogo → carrito → pedido con la Saga del capítulo 9) ya utilizable de principio a fin. No antes: montar una UI cuando el backend aún no tiene ni seguridad ni un flujo de negocio completo daría una demo a medias.
11. **Carga masiva / exportación de datos: Spring Batch** — caso de uso: importar un catálogo de productos desde un CSV de proveedor (alta masiva) y/o exportar periódicamente el catálogo a un sistema externo de reporting; job disparado vía endpoint REST o `@Scheduled`, sin servidor de orquestación aparte. **No Spring Cloud Data Flow**: Broadcom descatalogó su versión open-source en abril de 2025 (2.11.x fue la última release libre; desarrollo futuro solo para clientes Tanzu Spring de pago) — no tiene sentido enseñar una herramienta que el lector no podría usar sin licencia comercial.
12. **CI/CD: GitHub Actions** — pipeline de build+test (y despliegue, si procede) para el monorepo, ahora que hay suficiente superficie (varios servicios, Gateway, Batch) como para que compense automatizarlo.

Fuera de este plan (razonables pero no urgentes frente a lo anterior): Kubernetes/Helm, GitOps, test de contrato REST.

### Otros proyectos de Spring — candidatos anotados, sin comprometer todavía

Repaso de [spring.io/projects](https://spring.io/projects) cruzado con el plan anterior — encajarían sin forzar el dominio, pero no están comprometidos como capítulo propio hasta decidirlo al llegar al punto correspondiente:

- **Spring AI** — el dominio ya tiene `RecomendarProducto` (recomendaciones manuales entre productos); sustituir/complementar con embeddings + búsqueda semántica sobre el catálogo sería una extensión directa de una funcionalidad ya existente.
- **Spring Authorization Server** — nota comparativa candidata para el punto 8 (Seguridad): alternativa a Keycloak como Authorization Server hecho en Spring en vez de un producto externo, mismo patrón que la nota Zipkin/Tempo del punto 7.
- **Spring Session** — una vez haya Gateway (punto 9) y login real en Vaadin (punto 10), sesión distribuida (Redis) es la pieza que falta para que ese login funcione con más de una instancia detrás del Gateway.
- **Spring for GraphQL** — encaje natural en el propio Gateway/BFF (punto 9): agregar Catálogo + Pedidos bajo un único grafo, en vez de que el BFF sea solo un proxy REST.
- **Spring Modulith** — complementa o compite con ArchUnit+jMolecules del punto 2: verifica límites de módulos con su propio mecanismo (`ApplicationModules.of(...).verify()`).
- **Spring HATEOAS** — maduraría el nivel REST del capítulo 3 (ya cerrado); footnote candidata más que capítulo propio.
- **Spring gRPC** — alternativa al HTTP Service Client ya decidido en el punto 1; vale como nota comparativa, no como sustituto.
- **Spring Cloud Config** — activo (v5.0.4); config centralizada tiene sentido real en cuanto hay 2+ microservicios (punto 1) repitiendo `application.yml`.
- **Spring Cloud Vault** — activo (v5.0.2); ahora mismo las credenciales (Neo4j, etc.) viven en texto plano en `compose.yaml`/`application.yml` — encaja junto a Config y junto al capítulo de Seguridad (punto 8).

### Otras técnicas/herramientas candidatas, sin comprometer todavía

- **Gatling (test de carga)** — cierra el círculo de observabilidad: los puntos 5-6 montan Prometheus+Grafana pero nada genera tráfico real que valga la pena observar más allá de pruebas manuales. DSL Java/Kotlin nativo, encaja bien situado justo después del punto 6.

Descartados por forzados para este dominio o sin desarrollo futuro: Spring for Apache Pulsar (redundante tras Kafka+RabbitMQ del punto 3, un tercer binder no aporta nada nuevo), Spring LDAP, Spring Web Flow, Spring Web Services (SOAP), Spring Shell, Spring Cloud Netflix (hoy es solo Eureka; con Docker Compose y nombres de servicio fijos no hay *service discovery* dinámico que resolver), Spring Cloud Kubernetes (prerrequisito Kubernetes fuera de plan) y Spring Cloud Task (su valor real depende de Spring Cloud Data Flow, ya descartado; el punto 11 ya cubre el caso de uso batch sin servidor de orquestación).

## Arquitectura / DDD
- [x] Arquitectura Hexagonal (puertos de entrada/salida + adaptadores)
- [x] Agregado + Value Object (`Producto`, `Precio`, `ProductoId`)
- [ ] Entidades internas al agregado (distintas del propio agregado) — candidato natural: líneas de pedido dentro del agregado `Pedido` (capítulo 6, ver hoja de ruta), no necesita capítulo propio
- [x] Eventos de dominio — `ProductoCreadoEvento`/`RecomendacionAñadidaEvento` (capítulo 4), publicados con `ApplicationEventPublisher` desde los servicios de aplicación y consumidos con `@EventListener` síncrono dentro del mismo proceso; mecánica deliberadamente simple, sin broker externo (ver hoja de ruta para cuándo cruzan a otro proceso)
- [x] Relaciones de grafo (Categoría, recomendaciones de producto)
- [ ] jMolecules (anotaciones DDD)

## Persistencia
- [x] Spring Data Neo4j (`servicio-catalogo` — grafo)
- [ ] Migraciones/seed de datos — Flyway o Liquibase para el esquema relacional de `servicio-pedidos` (capítulo 6, ver hoja de ruta); el seed de datos de prueba usa la misma mecánica que la carga masiva del capítulo 16, a menor escala
- [ ] Persistencia políglota: cada microservicio futuro puede usar el motor más adecuado a su modelo (p. ej. relacional con Spring Data JPA/PostgreSQL, documental con MongoDB, caché con Redis) — se concreta motor a motor según el microservicio

## Herramientas de código
- [x] MapStruct (mappers dominio↔DTO, dominio↔entidad)
- [x] Lombok (`@RequiredArgsConstructor` en servicios/adaptadores/controller, `@Getter`+constructores en `ProductoEntidad`, y en el agregado `Producto`: `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` —solo por `id`— y `@Getter @Accessors(fluent = true)` para los getters de solo lectura, que no cambia la firma que ya usaban los mappers; el constructor de `Producto` sigue a mano porque valida invariantes, y en los Value Objects, que son `record`, tampoco aplica. Regla general para cualquier agregado futuro en `CLAUDE.md`)

## Comunicación entre servicios
- [ ] Cliente REST declarativo: HTTP Service Client de Spring Framework (`@HttpExchange` + `@ImportHttpServices`, sobre `RestClient`) — no OpenFeign: la documentación oficial de Spring Cloud OpenFeign lo da como "feature-complete" (solo bugfixes) y recomienda migrar a esta alternativa, ya soportada de forma nativa en Spring Boot 4.1 sin depender de Spring Cloud
- [ ] Mensajería asíncrona (Spring Cloud Stream, binders Kafka y RabbitMQ)
- [ ] API Gateway (Spring Cloud Gateway) — punto de entrada único para los microservicios expuestos

## Procesamiento por lotes
- [ ] Spring Batch (`Job`/`Step`, procesamiento por chunks) — carga masiva de productos desde CSV de proveedor y/o exportación periódica del catálogo a un sistema externo. No Spring Cloud Data Flow (descatalogado como open-source desde abril de 2025, ver hoja de ruta)

## Consistencia entre microservicios
- [ ] Patrón Saga (orquestación o coreografía, a decidir cuando haya al menos dos microservicios con transacción compartida — candidato natural: Pedidos + Pagos + Inventario)
- [ ] Métodos de compensación por cada paso de la Saga (deshacer lo ya confirmado si un paso posterior falla)

## Documentación de API
- [x] OpenAPI (Swagger) — especificación + UI interactiva (`springdoc-openapi`) para los siete endpoints REST de `servicio-catalogo` (capítulo 3). `@ApiResponses` documenta los códigos de estado reales que springdoc-openapi no puede inferir de la firma del método (`201`/`204` en vez del `200` por defecto, y las ramas `400`/`404` de `ControladorErroresGlobal`); `@Schema(example = "...")` en los DTOs de entrada para probar cada endpoint desde Swagger UI editando solo lo necesario; `@OpenAPIDefinition` para el título/versión de la API. Vaciar la base de datos entre pruebas manuales se documenta como una query Cypher (`MATCH (n) DETACH DELETE n` en Neo4j Browser), no como un endpoint nuevo — no hay caso de uso real detrás de esa operación
- [x] `ProblemDetail` (RFC 7807/9457) — respuestas de error estructuradas en `ControladorErroresGlobal` (capítulo 5), con `type`/`title` propios por excepción y extensiones (`productoId`/`categoriaId`) donde aporta un campo estructurable; sustituye el `ResponseEntity<String>` de texto plano de los capítulos 1-4. Anotaciones Swagger (`@Schema(implementation = ...)`) actualizadas de `String.class` a `ProblemDetail.class` en las respuestas `400`/`404`

## Frontend
- [ ] Vaadin (última versión estable en el momento de implementarlo) — UI web para consumir los microservicios

## Observabilidad
- [ ] Actuator + Micrometer
- [ ] Prometheus (scraping de métricas vía `micrometer-registry-prometheus`)
- [ ] Grafana (dashboards sobre las métricas de Prometheus)
- [ ] Trazas distribuidas (Micrometer Tracing, bridge OTel + Grafana Tempo)
- [ ] Logs estructurados centralizados

## Contenerización / Orquestación
- [x] Docker Compose (entorno de desarrollo)
- [ ] Kubernetes / Helm
- [ ] GitOps (ArgoCD/Flux)

## CI/CD
- [ ] GitHub Actions

## Testing
- [x] Test unitario de dominio (JUnit 5 + AssertJ)
- [x] Test de integración con Testcontainers (Neo4j)
- [ ] ArchUnit (reglas de capas)
- [ ] Test de contrato REST
- [ ] Instancio (rellenar objetos sin invariantes: `ProductoEntidad`, DTOs. **No** para el agregado `Producto` ni para `ProductoId`/`Precio` —tienen validación en el constructor y rellenarlos por reflexión se saltaría esas invariantes—; ahí mejor generar los valores primitivos con Instancio/DataFaker y pasarlos a `Producto.crear(...)`)
- [ ] DataFaker (datos realistas — nombre/descripción/precio de producto — para alimentar `Producto.crear(...)`; sustituto activo de Java Faker, sin releases desde 2020)

## Seguridad
- [ ] Autenticación/Autorización (OAuth2/Keycloak/JWT)

## Imágenes pendientes

Capturas de pantalla de interfaces web que el usuario debe adjuntar manualmente (los diagramas `.excalidraw` no cuentan aquí, esos se renderizan directamente a PNG). Carpeta destino: `docs/images/`. Formato: `.png`.

Las 3 capturas de Neo4j Browser del capítulo 1 (`neo4j-browser-login.png`, `neo4j-browser.png`, `neo4j-browser-grafo-productos.png`), la del capítulo 2 (`neo4j-browser-grafo-relaciones.png`), la del capítulo 3 (`swagger-ui.png`), las 2 del capítulo 4 (`intellij-maven-run-configuration.png`, `intellij-spring-boot-run-configuration.png`) y la del capítulo 5 (`swagger-ui-problemdetail.png`, capturada por Claude vía Playwright headless — Swagger UI es una página web navegable sin interacción de juicio, a diferencia de Neo4j Browser o IntelliJ) ya están adjuntadas. Ninguna pendiente por ahora.

## Microservicios candidatos
- [x] Catálogo/Productos (`servicio-catalogo`)
- [ ] Carrito/Pedidos
- [ ] Pagos
- [ ] Usuarios/Autenticación
- [ ] Inventario/Stock
- [ ] Notificaciones/Envíos
- [ ] API Gateway/BFF
