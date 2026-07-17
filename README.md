# Javacadabra — Tienda online por microservicios

[![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/25/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-6DB33F?logo=springboot&logoColor=white)](https://docs.spring.io/spring-boot/index.html)
[![Neo4j](https://img.shields.io/badge/Neo4j-4581C3?logo=neo4j&logoColor=white)](https://neo4j.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-FF6600?logo=rabbitmq&logoColor=white)](https://www.rabbitmq.com/)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-000000?logo=apachekafka&logoColor=white)](https://kafka.apache.org/)
[![OpenAPI](https://img.shields.io/badge/OpenAPI-3.0-6BA539?logo=swagger&logoColor=white)](https://swagger.io/specification/)
[![Docker Compose](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)](https://docs.docker.com/compose/)
[![Maven](https://img.shields.io/badge/Maven-Wrapper-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Licencia](https://img.shields.io/badge/Licencia-Apache%202.0-blue)](https://www.apache.org/licenses/LICENSE-2.0)

De cero a pro en arquitectura de microservicios con Spring Boot — una tienda online construida y documentada capítulo a capítulo, como un tutorial. Cada rama de Git es un capítulo; los capítulos son acumulativos (cada uno parte del anterior).

> **¿Por qué no hay código en esta rama?**
>
> `main` contiene únicamente este índice de capítulos: el código de cada microservicio vive en su propia rama de capítulo. Para explorar o ejecutar un capítulo concreto, haz `git checkout` a la rama correspondiente (p. ej. `git checkout capitulo-07-http-service-client`) — cada una incluye su propio `README.md` con las decisiones de diseño tomadas y cómo probarlo.

![Mapa de arquitectura de la tienda Javacadabra: servicio-catalogo (Neo4j) y servicio-pedidos (PostgreSQL) sobre Java/Spring Boot, conectados por HTTP Service Client; servicio-inventario (PostgreSQL, sin servidor web propio) consumiendo producto-creado vía RabbitMQ y pedido-creado vía Kafka, con hueco reservado para próximos capítulos](docs/images/mapa-arquitectura-sistema.png)

*Mapa de arquitectura del sistema — se amplía capítulo a capítulo a medida que se añaden microservicios y tecnologías nuevas.*

<br>

## Índice de capítulos

| # | Rama | Título | Microservicio | Conceptos clave |
|---|------|--------|----------------|------------------|
| 0 | [`capitulo-00-configuracion-inicial-spring-initializr`](../../tree/capitulo-00-configuracion-inicial-spring-initializr) | Configuración inicial: Spring Initializr, Lombok, MapStruct | — | Spring Initializr, Lombok, MapStruct |
| 1 | [`capitulo-01-fundamentos-ddd-hexagonal`](../../tree/capitulo-01-fundamentos-ddd-hexagonal) | Fundamentos DDD + Arquitectura Hexagonal | servicio-catalogo | DDD, Arquitectura Hexagonal, puertos y adaptadores |
| 2 | [`capitulo-02-relaciones-de-grafo-categoria`](../../tree/capitulo-02-relaciones-de-grafo-categoria) | Relaciones de grafo (Categoría) | servicio-catalogo | Neo4j, relaciones de grafo |
| 3 | [`capitulo-03-openapi-swagger`](../../tree/capitulo-03-openapi-swagger) | Documentación de la API con OpenAPI3/Swagger | servicio-catalogo | OpenAPI 3, Swagger UI |
| 4 | [`capitulo-04-eventos-dominio`](../../tree/capitulo-04-eventos-dominio) | Eventos de dominio (`ApplicationEventPublisher`/`@EventListener`) | servicio-catalogo | Eventos de dominio, `ApplicationEventPublisher` |
| 5 | [`capitulo-05-problemdetail-rfc7807`](../../tree/capitulo-05-problemdetail-rfc7807) | ProblemDetail (RFC 7807/9457) | servicio-catalogo | `ProblemDetail`, RFC 7807/9457 |
| 6 | [`capitulo-06-servicio-pedidos`](../../tree/capitulo-06-servicio-pedidos) | Segundo microservicio: Pedidos (JPA/PostgreSQL) | servicio-pedidos | JPA, PostgreSQL, Flyway |
| 7 | [`capitulo-07-http-service-client`](../../tree/capitulo-07-http-service-client) | Comunicación entre microservicios: Spring HTTP Service Client | servicio-pedidos | `@HttpExchange`, Capa Anticorrupción |
| 8 | [`capitulo-08-resiliencia`](../../tree/capitulo-08-resiliencia) | Resiliencia de la llamada síncrona | servicio-pedidos | `@Retryable`, `@ConcurrencyLimit`, Circuit Breaker (Resilience4j) |
| 9 | [`capitulo-09-test-capa-web`](../../tree/capitulo-09-test-capa-web) | Test de la capa web | servicio-catalogo, servicio-pedidos | `MockMvcTester`, `RestTestClient` |
| 10 | [`capitulo-10-archunit-jmolecules`](../../tree/capitulo-10-archunit-jmolecules) | Disciplina de tests y arquitectura: ArchUnit, jMolecules, Instancio y DataFaker | servicio-catalogo, servicio-pedidos | ArchUnit, jMolecules, Instancio, DataFaker |
| 11 | [`capitulo-11-mensajeria-asincrona`](../../tree/capitulo-11-mensajeria-asincrona) | Mensajería asíncrona: Spring Cloud Stream con RabbitMQ | servicio-catalogo | Spring Cloud Stream, Binder, exchange/cola de RabbitMQ |
| 12 | [`capitulo-12-outbox-transaccional`](../../tree/capitulo-12-outbox-transaccional) | Kafka como segundo binder, Outbox transaccional e Inventario | servicio-catalogo, servicio-pedidos, servicio-inventario | Kafka, Outbox transaccional, consumidores idempotentes |

Consulta el [`CHECKLIST.md`](../../blob/capitulo-12-outbox-transaccional/CHECKLIST.md) de la rama del último capítulo para ver el detalle de tecnologías cubiertas y pendientes. Cada rama de capítulo incluye su propio `README.md` con las decisiones de diseño tomadas y cómo probarlo.

## Stack tecnológico

Versiones fijadas explícitamente como propiedad en el `pom.xml` raíz — cada fila enlaza al capítulo donde se introdujo:

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 25 | Lenguaje base |
| Spring Boot | 4.1.0 | Framework de aplicación (BOM padre del monorepo) |
| Spring Cloud | 2025.1.2 | BOM de dependencias inter-servicio |
| MapStruct | 1.6.3 | Mapeo dominio↔DTO, dominio↔entidad |
| Testcontainers | 1.21.4 | Tests de integración con infraestructura real |
| [springdoc-openapi](../../tree/capitulo-03-openapi-swagger) | 3.0.3 | OpenAPI 3 / Swagger UI |
| [Resilience4j](../../tree/capitulo-08-resiliencia) | 2.3.0 | Circuit breaker (`servicio-pedidos`) |
| [ArchUnit](../../tree/capitulo-10-archunit-jmolecules) | 1.4.1 | Reglas de arquitectura como test ejecutable |
| [jMolecules](../../tree/capitulo-10-archunit-jmolecules) | 1.10.0 | Anotaciones DDD / Arquitectura Hexagonal (metadato puro, verificado por `jmolecules-archunit`) |
| [Instancio](../../tree/capitulo-10-archunit-jmolecules) | 5.6.0 | Generación de datos de prueba (objetos sin invariantes) |
| [DataFaker](../../tree/capitulo-10-archunit-jmolecules) | 2.7.0 | Generación de datos de prueba realistas (Objetos de Valor validados) |

Versiones sin propiedad propia: las resuelve el BOM de `spring-boot-starter-parent` (suben solas al actualizar la versión de Spring Boot):

| Tecnología | Versión resuelta | Uso |
|---|---|---|
| Lombok | 1.18.46 | Reduce boilerplate (getters, constructores) |
| Spring Framework | 7.0.8 | Núcleo de Spring Boot 4.1 (Spring MVC, Spring Web) |
| [Spring Data Neo4j](../../tree/capitulo-01-fundamentos-ddd-hexagonal) | 8.1.0 | Persistencia en grafo (`servicio-catalogo`) |
| [Spring Data JPA](../../tree/capitulo-06-servicio-pedidos) | 4.1.0 | Persistencia relacional (`servicio-pedidos`, `servicio-inventario`) |
| [Flyway](../../tree/capitulo-06-servicio-pedidos) | 12.4.0 | Migraciones de esquema (`servicio-pedidos`, `servicio-inventario`) |
| [Spring Cloud Stream](../../tree/capitulo-12-outbox-transaccional) | 5.0.2 | Mensajería asíncrona, binders RabbitMQ y Kafka intercambiables/combinables por *binding* |
| PostgreSQL JDBC driver | 42.7.11 | Driver de conexión (`servicio-pedidos`) |
| Docker Compose | — | Entorno de desarrollo local |

Ver [CLAUDE.md](CLAUDE.md) para la guía completa de convenciones (idioma, arquitectura, modelo de ramas).
