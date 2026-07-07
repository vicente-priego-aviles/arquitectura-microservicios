# Checklist tecnológico

Estado de las tecnologías/temas cubiertos en el tutorial, capítulo a capítulo. Vive en la rama del último capítulo (no en `main`) y se actualiza en cada capítulo nuevo.

## Hoja de ruta de próximos capítulos (tentativa)

Orden recomendado para los capítulos 4 en adelante, sujeto a revisión al planificar cada uno (como el resto del tutorial, el alcance final de cada capítulo se decide en conversación, no de antemano).

1. **Eventos de dominio** — en proceso, vía `ApplicationEventPublisher`/`@EventListener` de Spring; separa el concepto de "evento de dominio" de la mecánica de transporte antes de ir a mensajería distribuida.
2. **Segundo microservicio: Carrito/Pedidos** — persistencia políglota (Spring Data JPA/PostgreSQL, para contrastar con el grafo de Neo4j) y llamada síncrona a `servicio-catalogo` vía HTTP Service Client de Spring (`@HttpExchange`/`@ImportHttpServices`, no OpenFeign — ver "Comunicación entre servicios" más abajo).
3. **Disciplina de tests y arquitectura: ArchUnit + jMolecules + Instancio + DataFaker** — se pospone hasta tener dos microservicios porque el valor de automatizar la disciplina hexagonal (ArchUnit sobre anotaciones jMolecules, vía `jmolecules-archunit`) y de reducir boilerplate de fixtures (Instancio/DataFaker) crece con el número de servicios/agregados a mantener consistentes; con uno solo no compensa el coste de introducirlas.
4. **Mensajería asíncrona (Kafka)** — los eventos de dominio del capítulo 4 salen del proceso; los consume el microservicio del capítulo 5.
5. **Patrón Saga + compensación** — con Pedidos+Catálogo (y quizá Pagos) compartiendo una transacción de negocio.
6. **Observabilidad** (Actuator + Micrometer + Prometheus + Grafana) — con tráfico real entre servicios y mensajería de por medio, hay algo que observar.
7. **Seguridad: OAuth2/Keycloak/JWT** — proteger los endpoints de ambos microservicios antes de exponer nada a un frontend real o a un gateway público.
8. **API Gateway/BFF** (Spring Cloud Gateway) — punto de entrada único; centraliza la autenticación del capítulo anterior en vez de repetirla en cada microservicio.
9. **Frontend: Vaadin** — consume el Gateway, con login real y el flujo de negocio completo (catálogo → carrito → pedido con la Saga del capítulo 8) ya utilizable de principio a fin. No antes: montar una UI cuando el backend aún no tiene ni seguridad ni un flujo de negocio completo daría una demo a medias.
10. **Carga masiva / exportación de datos: Spring Batch** — caso de uso: importar un catálogo de productos desde un CSV de proveedor (alta masiva) y/o exportar periódicamente el catálogo a un sistema externo de reporting; job disparado vía endpoint REST o `@Scheduled`, sin servidor de orquestación aparte. **No Spring Cloud Data Flow**: Broadcom descatalogó su versión open-source en abril de 2025 (2.11.x fue la última release libre; desarrollo futuro solo para clientes Tanzu Spring de pago) — no tiene sentido enseñar una herramienta que el lector no podría usar sin licencia comercial.
11. **CI/CD: GitHub Actions** — pipeline de build+test (y despliegue, si procede) para el monorepo, ahora que hay suficiente superficie (varios servicios, Gateway, Batch) como para que compense automatizarlo.

Fuera de este plan (razonables pero no urgentes frente a lo anterior): Kubernetes/Helm, GitOps, trazas distribuidas/logs centralizados, test de contrato REST.

## Arquitectura / DDD
- [x] Arquitectura Hexagonal (puertos de entrada/salida + adaptadores)
- [x] Agregado + Value Object (`Producto`, `Precio`, `ProductoId`)
- [ ] Entidades internas al agregado (distintas del propio agregado) — candidato natural: líneas de pedido dentro del agregado `Pedido` (capítulo 5, ver hoja de ruta), no necesita capítulo propio
- [ ] Eventos de dominio
- [x] Relaciones de grafo (Categoría, recomendaciones de producto)
- [ ] jMolecules (anotaciones DDD)

## Persistencia
- [x] Spring Data Neo4j (`servicio-catalogo` — grafo)
- [ ] Migraciones/seed de datos — misma mecánica que la carga masiva del capítulo 13 (ver hoja de ruta) a menor escala
- [ ] Persistencia políglota: cada microservicio futuro puede usar el motor más adecuado a su modelo (p. ej. relacional con Spring Data JPA/PostgreSQL, documental con MongoDB, caché con Redis) — se concreta motor a motor según el microservicio

## Herramientas de código
- [x] MapStruct (mappers dominio↔DTO, dominio↔entidad)
- [x] Lombok (`@RequiredArgsConstructor` en servicios/adaptadores/controller, `@Getter`+constructores en `ProductoEntidad`, y en el agregado `Producto`: `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` —solo por `id`— y `@Getter @Accessors(fluent = true)` para los getters de solo lectura, que no cambia la firma que ya usaban los mappers; el constructor de `Producto` sigue a mano porque valida invariantes, y en los Value Objects, que son `record`, tampoco aplica. Regla general para cualquier agregado futuro en `CLAUDE.md`)

## Comunicación entre servicios
- [ ] Cliente REST declarativo: HTTP Service Client de Spring Framework (`@HttpExchange` + `@ImportHttpServices`, sobre `RestClient`) — no OpenFeign: la documentación oficial de Spring Cloud OpenFeign lo da como "feature-complete" (solo bugfixes) y recomienda migrar a esta alternativa, ya soportada de forma nativa en Spring Boot 4.1 sin depender de Spring Cloud
- [ ] Mensajería asíncrona (Kafka/RabbitMQ)
- [ ] API Gateway (Spring Cloud Gateway) — punto de entrada único para los microservicios expuestos

## Procesamiento por lotes
- [ ] Spring Batch (`Job`/`Step`, procesamiento por chunks) — carga masiva de productos desde CSV de proveedor y/o exportación periódica del catálogo a un sistema externo. No Spring Cloud Data Flow (descatalogado como open-source desde abril de 2025, ver hoja de ruta)

## Consistencia entre microservicios
- [ ] Patrón Saga (orquestación o coreografía, a decidir cuando haya al menos dos microservicios con transacción compartida — candidato natural: Pedidos + Pagos + Inventario)
- [ ] Métodos de compensación por cada paso de la Saga (deshacer lo ya confirmado si un paso posterior falla)

## Documentación de API
- [x] OpenAPI (Swagger) — especificación + UI interactiva (`springdoc-openapi`) para los siete endpoints REST de `servicio-catalogo` (capítulo 3). `@ApiResponses` documenta los códigos de estado reales que springdoc-openapi no puede inferir de la firma del método (`201`/`204` en vez del `200` por defecto, y las ramas `400`/`404` de `ControladorErroresGlobal`); `@Schema(example = "...")` en los DTOs de entrada para probar cada endpoint desde Swagger UI editando solo lo necesario; `@OpenAPIDefinition` para el título/versión de la API. Vaciar la base de datos entre pruebas manuales se documenta como una query Cypher (`MATCH (n) DETACH DELETE n` en Neo4j Browser), no como un endpoint nuevo — no hay caso de uso real detrás de esa operación

## Frontend
- [ ] Vaadin (última versión estable en el momento de implementarlo) — UI web para consumir los microservicios

## Observabilidad
- [ ] Actuator + Micrometer
- [ ] Prometheus (scraping de métricas vía `micrometer-registry-prometheus`)
- [ ] Grafana (dashboards sobre las métricas de Prometheus)
- [ ] Trazas distribuidas (OpenTelemetry/Zipkin)
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

Las 3 capturas de Neo4j Browser del capítulo 1 (`neo4j-browser-login.png`, `neo4j-browser.png`, `neo4j-browser-grafo-productos.png`) y la del capítulo 2 (`neo4j-browser-grafo-relaciones.png`) ya están adjuntadas.

Pendiente: `docs/images/capitulo-03/swagger-ui.png` — captura de `http://localhost:8080/swagger-ui.html` con `servicio-catalogo` arrancado, mostrando los endpoints de `producto-controller` y `categoria-controller` agrupados por tag.

## Microservicios candidatos
- [x] Catálogo/Productos (`servicio-catalogo`)
- [ ] Carrito/Pedidos
- [ ] Pagos
- [ ] Usuarios/Autenticación
- [ ] Inventario/Stock
- [ ] Notificaciones/Envíos
- [ ] API Gateway/BFF
