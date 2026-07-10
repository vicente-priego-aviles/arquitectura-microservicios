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

Ver `CLAUDE.md`, sección "Idioma y lenguaje ubicuo", para cómo se aplica esta convención.
