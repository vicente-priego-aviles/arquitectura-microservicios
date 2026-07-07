# Checklist tecnológico

Estado de las tecnologías/temas cubiertos en el tutorial, capítulo a capítulo. Vive en la rama del último capítulo (no en `main`) y se actualiza en cada capítulo nuevo.

## Arquitectura / DDD
- [x] Arquitectura Hexagonal (puertos de entrada/salida + adaptadores)
- [x] Agregado + Value Object (`Producto`, `Precio`, `ProductoId`)
- [ ] Entidades internas al agregado (distintas del propio agregado)
- [ ] Eventos de dominio
- [x] Relaciones de grafo (Categoría, recomendaciones de producto)
- [ ] jMolecules (anotaciones DDD)

## Persistencia
- [x] Spring Data Neo4j (`servicio-catalogo` — grafo)
- [ ] Migraciones/seed de datos
- [ ] Persistencia políglota: cada microservicio futuro puede usar el motor más adecuado a su modelo (p. ej. relacional con Spring Data JPA/PostgreSQL, documental con MongoDB, caché con Redis) — se concreta motor a motor según el microservicio

## Herramientas de código
- [x] MapStruct (mappers dominio↔DTO, dominio↔entidad)
- [x] Lombok (`@RequiredArgsConstructor` en servicios/adaptadores/controller, `@Getter`+constructores en `ProductoEntidad`, y en el agregado `Producto`: `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` —solo por `id`— y `@Getter @Accessors(fluent = true)` para los getters de solo lectura, que no cambia la firma que ya usaban los mappers; el constructor de `Producto` sigue a mano porque valida invariantes, y en los Value Objects, que son `record`, tampoco aplica. Regla general para cualquier agregado futuro en `CLAUDE.md`)

## Comunicación entre servicios
- [ ] Cliente REST / OpenFeign
- [ ] Mensajería asíncrona (Kafka/RabbitMQ)

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
