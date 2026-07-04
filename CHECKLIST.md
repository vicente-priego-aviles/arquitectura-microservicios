# Checklist tecnológico

Estado de las tecnologías/temas cubiertos en el tutorial, capítulo a capítulo. Vive en la rama del último capítulo (no en `main`) y se actualiza en cada capítulo nuevo.

## Arquitectura / DDD
- [x] Arquitectura Hexagonal (puertos de entrada/salida + adaptadores)
- [x] Agregado + Value Object (`Producto`, `Precio`, `ProductoId`)
- [ ] Entidades internas al agregado (distintas del propio agregado)
- [ ] Eventos de dominio
- [ ] Relaciones de grafo (Categoría, recomendaciones de producto)
- [ ] jMolecules (anotaciones DDD)

## Persistencia
- [x] Spring Data Neo4j
- [ ] Migraciones/seed de datos

## Herramientas de código
- [x] MapStruct (mappers dominio↔DTO, dominio↔entidad)
- [x] Lombok (`@RequiredArgsConstructor` en servicios/adaptadores/controller, `@Getter`+constructores en `ProductoEntidad`, `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` en el agregado `Producto` —solo por `id`—; el constructor de `Producto` sigue a mano porque valida invariantes, y en los Value Objects, que son `record`, tampoco aplica)

## Comunicación entre servicios
- [ ] Cliente REST / OpenFeign
- [ ] Mensajería asíncrona (Kafka/RabbitMQ)

## Observabilidad
- [ ] Actuator + Micrometer
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

## Microservicios candidatos
- [x] Catálogo/Productos (`servicio-catalogo`)
- [ ] Carrito/Pedidos
- [ ] Pagos
- [ ] Usuarios/Autenticación
- [ ] Inventario/Stock
- [ ] Notificaciones/Envíos
- [ ] API Gateway/BFF
