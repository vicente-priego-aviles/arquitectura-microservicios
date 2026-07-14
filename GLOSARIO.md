# Glosario de términos técnicos (español ↔ inglés)

Documentación interna de desarrollo (como `CLAUDE.md`/`CHECKLIST.md`): no forma parte del contenido dirigido al lector del tutorial, sino de la convención de traducción que debe aplicarse de forma consistente en el `README.md` de cada capítulo.

## Convención

- La primera vez que un término técnico aparece en el `README.md` de un capítulo, se escribe primero su traducción al español (la forma que se usará el resto del documento) y, entre paréntesis justo a continuación, el término original en inglés — p. ej. **"Objeto de Valor (Value Object)"**.
- A partir de esa primera aparición, el resto del documento usa solo la forma en español ya fijada, sin repetir el paréntesis.
- Si un término no tiene una traducción española de uso consolidado en la literatura DDD/arquitectura, se mantiene el término en inglés tal cual (columna "Español" a `—` en la tabla), sin forzar una traducción artificial.
- Cada vez que un capítulo nuevo introduzca un término técnico que no esté todavía en esta tabla, se añade una fila aquí en el mismo commit que lo documenta en el `README.md`.

## Términos

| Español | Inglés | Notas |
|---|---|---|
| — | Domain-Driven Design (DDD) | Sin traducción corta de uso consolidado; se mantiene la sigla `DDD` como término principal |
| Lenguaje ubicuo | Ubiquitous Language | |
| Objeto de Valor | Value Object | |
| Entidad | Entity | Entidad interna al agregado, distinta del propio Agregado |
| Agregado | Aggregate | |
| Arquitectura Hexagonal | Hexagonal Architecture (Ports & Adapters) | |
| Puerto de entrada | Inbound Port | |
| Puerto de salida | Outbound Port | |
| Servicio de aplicación | Application Service | |
| Caso de uso | Use Case | |
| Adaptador | Adapter | Cognado directo; no requiere glosa en el texto |
| Repositorio | Repository | Cognado directo; no requiere glosa en el texto |
| Evento de Dominio | Domain Event | |
| Detalles del Problema | Problem Details | RFC 9457 (anteriormente RFC 7807); implementado en Spring como la clase `ProblemDetail` |
| Contexto Delimitado | Bounded Context | |
| Persistencia políglota | Polyglot Persistence | |
| Migración de esquema | Schema Migration | Herramientas: Flyway (SQL plano) o Liquibase (XML/YAML/JSON) |
| Capa Anticorrupción | Anti-Corruption Layer | Traduce la respuesta de un Contexto Delimitado ajeno (p. ej. `servicio-catalogo`) a un tipo propio del Contexto que la consume, en vez de propagar el contrato externo tal cual |
| Reintento | Retry | Mecanismo de resiliencia que repite una operación fallida un número acotado de veces, normalmente con espera creciente (backoff exponencial) entre intentos, antes de propagar el fallo definitivamente |
| — | Backoff | Estrategia de espera creciente entre reintentos sucesivos (típicamente duplicando el tiempo de espera en cada intento, "backoff exponencial"); sin traducción española de uso consolidado |
| Límite de concurrencia | Concurrency Limit | Acota cuántas invocaciones de un método pueden estar en curso a la vez; variante del patrón clásico Bulkhead (compartimento estanco), sin traducción española de uso consolidado |
| — | Circuit Breaker | Máquina de estados (`CLOSED`/`OPEN`/`HALF_OPEN`) que deja de invocar un servicio que falla de forma sostenida, para no insistir contra algo que ya se sabe que no responde; sin traducción española de uso consolidado en la literatura de resiliencia |
| Método de reserva | Fallback Method | Método alternativo invocado cuando la llamada protegida falla o el circuito está abierto, en vez de propagar la excepción original |
| — | Jitter | Variación aleatoria añadida a cada espera de un backoff exponencial, para que reintentos que fallaron a la vez no se sincronicen también al reintentar; sin traducción española de uso consolidado |
| — | Test Slice | Carga solo la porción del contexto de Spring relacionada con una capa concreta (p. ej. `@WebMvcTest` carga solo la infraestructura MVC), sustituyendo el resto de colaboradores por dobles de prueba en vez de arrancar el contexto completo; sin traducción española de uso consolidado |
| Configuración autónoma | Standalone Setup | Instancia el controlador a mano, sin contexto de Spring ni escaneo de componentes (`RestTestClient.bindToController(...)`); cualquier `@ControllerAdvice` debe registrarse explícitamente porque no hay escaneo que lo descubra |
| — | Binder | Spring Cloud Stream: adaptador entre el modelo de programación de Spring (`Supplier`/`Function`/`Consumer` como `@Bean`) y la API concreta de un broker (Kafka, RabbitMQ); cambiar de binder es cambiar configuración, no código de negocio. Sin traducción española de uso consolidado |
| — | Binding | Spring Cloud Stream: conexión configurable entre un bean funcional (`Supplier`/`Function`/`Consumer`) y un destino concreto del broker, nombrada `<bean>-in-<índice>`/`<bean>-out-<índice>`; sin traducción española de uso consolidado |
| — | Testcontainers | Librería Java que levanta contenedores Docker reales (bases de datos, brokers de mensajería...) solo durante la ejecución de un test, y los destruye al terminar; sin traducción española de uso consolidado |
| Cola | Queue | En RabbitMQ, almacena los mensajes que un *exchange* le enruta hasta que un consumidor los retira; a diferencia del *exchange* (que no almacena nada), la cola sí persiste el mensaje mientras no se consuma |
| Origen | Source | Spring Cloud Stream: rol de un `Supplier<T>` como `@Bean` — el binder lo invoca y publica cada valor que devuelve |
| Procesador | Processor | Spring Cloud Stream: rol de un `Function<T, R>` como `@Bean` — el binder le entrega un mensaje consumido y publica el resultado que devuelve |
| Destino | Sink | Spring Cloud Stream: rol de un `Consumer<T>` como `@Bean` — el binder le entrega un mensaje consumido y no publica nada a cambio |
| Outbox transaccional | Transactional Outbox | Patrón: guardar el evento a publicar en la misma transacción que el cambio de estado que lo origina (tabla outbox), y publicarlo aparte con un proceso que sondea esa tabla (poller) — evita el problema de doble escritura cuando publicar en el broker ya no es atómico con el commit en base de datos |
| Idempotencia | Idempotency | Capacidad de un consumidor de procesar el mismo mensaje más de una vez (por una redelivery del broker) sin duplicar su efecto de negocio, normalmente deduplicando por el id del propio evento |
| — | Topic | En Kafka, el equivalente funcional a la pareja *exchange*+cola de RabbitMQ, pero en una sola pieza: un productor escribe directamente a un topic con nombre, que es también quien almacena — un log de solo-anexar que no borra el mensaje al consumirlo, a diferencia de la cola clásica de RabbitMQ; sin traducción española de uso consolidado |
| — | Poller | Proceso que sondea (polls) periódicamente una fuente en busca de trabajo pendiente — aquí, una tabla outbox —, en vez de reaccionar a una notificación push; sin traducción española de uso consolidado |

Ver `CLAUDE.md`, sección "Idioma y lenguaje ubicuo", para cómo se aplica esta convención.
