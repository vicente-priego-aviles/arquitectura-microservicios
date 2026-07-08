# arquitectura-microservicios

De cero a pro en arquitectura de microservicios con Spring Boot — una tienda online construida y documentada capítulo a capítulo, como un tutorial. Cada rama de Git es un capítulo; los capítulos son acumulativos (cada uno parte del anterior).

## Índice de capítulos

| # | Rama | Título | Microservicio | Estado |
|---|------|--------|----------------|--------|
| 0 | [`capitulo-00-configuracion-inicial-spring-initializr`](../../tree/capitulo-00-configuracion-inicial-spring-initializr) | Configuración inicial: Spring Initializr, Lombok, MapStruct | — | ✅ |
| 1 | [`capitulo-01-fundamentos-ddd-hexagonal`](../../tree/capitulo-01-fundamentos-ddd-hexagonal) | Fundamentos DDD + Arquitectura Hexagonal | servicio-catalogo | ✅ |
| 2 | [`capitulo-02-relaciones-de-grafo-categoria`](../../tree/capitulo-02-relaciones-de-grafo-categoria) | Relaciones de grafo (Categoría) | servicio-catalogo | ✅ |
| 3 | [`capitulo-03-openapi-swagger`](../../tree/capitulo-03-openapi-swagger) | Documentación de la API con OpenAPI3/Swagger | servicio-catalogo | ✅ |
| 4 | [`capitulo-04-eventos-dominio`](../../tree/capitulo-04-eventos-dominio) | Eventos de dominio (`ApplicationEventPublisher`/`@EventListener`) | servicio-catalogo | ✅ |

Consulta el `CHECKLIST.md` de la rama del último capítulo para ver el detalle de tecnologías cubiertas y pendientes. Cada rama de capítulo incluye su propio `README.md` con las decisiones de diseño tomadas y cómo probarlo.

Ver [CLAUDE.md](CLAUDE.md) para la guía completa de convenciones (idioma, arquitectura, modelo de ramas).
