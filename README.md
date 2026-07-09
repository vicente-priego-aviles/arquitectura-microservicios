# arquitectura-microservicios

[![Java](https://img.shields.io/badge/Java-25-orange?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/25/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-6DB33F?logo=springboot&logoColor=white)](https://docs.spring.io/spring-boot/index.html)
[![Licencia](https://img.shields.io/badge/Licencia-Apache%202.0-blue)](https://www.apache.org/licenses/LICENSE-2.0)

De cero a pro en arquitectura de microservicios con Spring Boot — una tienda online construida y documentada capítulo a capítulo, como un tutorial. Cada rama de Git es un capítulo; los capítulos son acumulativos (cada uno parte del anterior).

## Índice de capítulos

| # | Rama | Título | Microservicio | Estado |
|---|------|--------|----------------|--------|
| 0 | [`capitulo-00-configuracion-inicial-spring-initializr`](../../tree/capitulo-00-configuracion-inicial-spring-initializr) | Configuración inicial: Spring Initializr, Lombok, MapStruct | — | ✅ |
| 1 | [`capitulo-01-fundamentos-ddd-hexagonal`](../../tree/capitulo-01-fundamentos-ddd-hexagonal) | Fundamentos DDD + Arquitectura Hexagonal | servicio-catalogo | ✅ |
| 2 | [`capitulo-02-relaciones-de-grafo-categoria`](../../tree/capitulo-02-relaciones-de-grafo-categoria) | Relaciones de grafo (Categoría) | servicio-catalogo | ✅ |
| 3 | [`capitulo-03-openapi-swagger`](../../tree/capitulo-03-openapi-swagger) | Documentación de la API con OpenAPI3/Swagger | servicio-catalogo | ✅ |
| 4 | [`capitulo-04-eventos-dominio`](../../tree/capitulo-04-eventos-dominio) | Eventos de dominio (`ApplicationEventPublisher`/`@EventListener`) | servicio-catalogo | ✅ |
| 5 | [`capitulo-05-problemdetail-rfc7807`](../../tree/capitulo-05-problemdetail-rfc7807) | ProblemDetail (RFC 7807/9457) | servicio-catalogo | ✅ |
| 6 | [`capitulo-06-servicio-pedidos`](../../tree/capitulo-06-servicio-pedidos) | Segundo microservicio: Pedidos (JPA/PostgreSQL) | servicio-pedidos | ✅ |

Consulta el `CHECKLIST.md` de la rama del último capítulo para ver el detalle de tecnologías cubiertas y pendientes. Cada rama de capítulo incluye su propio `README.md` con las decisiones de diseño tomadas y cómo probarlo.

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

Versiones sin propiedad propia: las resuelve el BOM de `spring-boot-starter-parent` (suben solas al actualizar la versión de Spring Boot):

| Tecnología | Versión resuelta | Uso |
|---|---|---|
| Lombok | 1.18.46 | Reduce boilerplate (getters, constructores) |
| Spring Framework | 7.0.8 | Núcleo de Spring Boot 4.1 (Spring MVC, Spring Web) |
| [Spring Data Neo4j](../../tree/capitulo-01-fundamentos-ddd-hexagonal) | 8.1.0 | Persistencia en grafo (`servicio-catalogo`) |
| [Spring Data JPA](../../tree/capitulo-06-servicio-pedidos) | 4.1.0 | Persistencia relacional (`servicio-pedidos`) |
| [Flyway](../../tree/capitulo-06-servicio-pedidos) | 12.4.0 | Migraciones de esquema (`servicio-pedidos`) |
| PostgreSQL JDBC driver | 42.7.11 | Driver de conexión (`servicio-pedidos`) |
| Docker Compose | — | Entorno de desarrollo local |

Ver [CLAUDE.md](CLAUDE.md) para la guía completa de convenciones (idioma, arquitectura, modelo de ramas).
