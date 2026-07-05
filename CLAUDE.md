# CLAUDE.md

Este archivo guía a Claude Code (claude.ai/code) cuando trabaja con el código de este repositorio.

## Qué es este proyecto

Monorepo de aprendizaje: una tienda online construida microservicio a microservicio con Spring Boot, DDD y Clean/Hexagonal Architecture, documentada como un tutorial donde **cada rama de Git es un capítulo**. El objetivo no es solo llegar a un producto final, sino dejar por el camino una guía didáctica de arquitectura de microservicios (DDD, observabilidad, Docker, Kubernetes, GitOps, GitHub Actions, tests, etc.).

La rama `main` contiene **únicamente el índice general de capítulos** (este `CLAUDE.md` y el `README.md` con la tabla de ramas). No contiene código de ningún microservicio: el código vive en las ramas de capítulo.

## Modelo de ramas (acumulativo)

- Cada capítulo es una rama nombrada `capitulo-NN-slug-en-espanol` (p. ej. `capitulo-01-fundamentos-ddd-hexagonal`).
- Los capítulos son **acumulativos**: cada rama nueva parte de la rama del capítulo anterior, no de `main`.
  ```bash
  git checkout <rama-del-capitulo-anterior>
  git checkout -b capitulo-NN-slug-en-espanol
  ```
- Al terminar un capítulo se comitea en su rama y se actualiza el índice de `main` (tabla de capítulos en `README.md`) con un commit propio en `main`.
- El `CHECKLIST.md` (tecnologías cubiertas/pendientes) vive en la rama del último capítulo, no en `main`; cada capítulo lo actualiza como parte de su commit.
- Cada rama de capítulo lleva su propio `README.md` en la raíz explicando qué se construyó, las decisiones de diseño tomadas y cómo probarlo.

## Idioma y lenguaje ubicuo

- Todo el contenido que se genera (documentación, commits, comentarios, nombres del modelo de dominio) debe estar **en español**.
- El **modelo de dominio** (agregados, entidades, value objects, excepciones de dominio, eventos) usa nombres en español siguiendo el lenguaje ubicuo DDD: `Producto`, `Precio`, `ProductoNoEncontradoExcepcion`, etc.
- Las **capas técnicas/framework** pueden mantener sufijos en inglés cuando es el estándar (`Controller`, `Repository`, `Service`, `DTO`), combinados con el nombre de dominio en español (`ProductoController`, `ProductoRepositorioAdaptador`).

## Convención arquitectónica: Hexagonal + DDD

Esta convención es compartida con el proyecto hermano `sistema-reservas-viaje` del mismo autor, para que ambos "hablen el mismo idioma arquitectónico".

| Capa | Paquete | Sufijo/patrón |
|---|---|---|
| Dominio — agregados | `dominio.modelo.agregado` | — |
| Dominio — value objects | `dominio.modelo.objetovalor` | `record` |
| Dominio — entidades internas | `dominio.modelo.entidad` | — |
| Dominio — excepciones | `dominio.excepcion` | `...Excepcion` |
| Dominio — eventos | `dominio.evento` | `...Evento` |
| Aplicación — puertos de entrada (casos de uso) | `aplicacion.puerto.entrada` | `...PuertoEntrada` |
| Aplicación — puertos de salida | `aplicacion.puerto.salida` | `...PuertoSalida` |
| Aplicación — servicios (implementan los puertos de entrada) | `aplicacion.servicio` | `...Servicio` |
| Aplicación — DTOs | `aplicacion.dto.entrada` / `aplicacion.dto.salida` | `...DTO` |
| Aplicación — mappers | `aplicacion.mapper` | MapStruct |
| Infraestructura — adaptadores de entrada (REST, etc.) | `infraestructura.adaptador.entrada.rest` | `...Controller` |
| Infraestructura — adaptadores de salida (persistencia) | `infraestructura.adaptador.salida.persistencia.{entidad,repositorio,mapper,adaptador}` | `...Entidad`, `...RepositorioNeo4j`, `...Adaptador` |

Regla clave: el `dominio` no conoce nada de frameworks ni de persistencia; los puertos de salida son la única forma en que `aplicacion` habla con `infraestructura`.

## Convención de Lombok en agregados

Aplica a **todo agregado raíz** (`dominio.modelo.agregado`) de cualquier microservicio del monorepo, presente o futuro. La pregunta que decide si una anotación de Lombok es segura no es "¿es dominio o infraestructura?", sino **"¿la anotación puede generar código que se salte una invariante de negocio?"**:

- **Constructor**: siempre escrito a mano — `private` + factories estáticas nombradas (`crear` para alta con validación completa, `reconstruir` para rehidratar desde persistencia sin repetir esa validación). Nunca `@AllArgsConstructor`/`@NoArgsConstructor` en el agregado: aunque el constructor privado en sí mismo no valide nada hoy, mañana esa validación puede trasladarse ahí, y una anotación no puede alojar lógica.
- **Getters de solo lectura**: sí, vía `@Getter` + `@Accessors(fluent = true)` en la clase. Un getter no puede saltarse ninguna invariante. `@Accessors(fluent = true)` es obligatorio para mantener el estilo de accesor sin prefijo `get` (`id()`, `nombre()`, no `getId()`), consistente con los Value Objects (que son `record`) y sin exigir ningún cambio en los mappers MapStruct que ya llaman a esos accesores.
- **`equals`/`hashCode`**: sí, vía `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` en la clase + `@EqualsAndHashCode.Include` solo en el campo de identidad (el id del agregado). Un agregado se compara por identidad, nunca por sus demás campos.
- **Nunca `@Data` ni `@Value`**: `@Value` en concreto genera un constructor **público** con todos los campos, lo que permitiría construir el agregado sin pasar por `crear`/`reconstruir` — exactamente la invariante que este patrón protege. Además fuerza todos los campos a `final`, lo que puede chocar con campos mutables del agregado (p. ej. un precio que se actualiza con un método de dominio).

Fue el caso aplicado en `servicio-catalogo` sobre `Producto` (ver su `README.md` de capítulo, sección 5, para el razonamiento completo con ejemplos de código) — aplícalo igual en cualquier agregado nuevo de capítulos futuros.

## Estructura del monorepo

El `pom.xml` raíz es el **parent multi-módulo** (`packaging=pom`): centraliza versiones de Java/Spring Cloud, `dependencyManagement` y la configuración común de plugins (`spring-boot-maven-plugin`, `maven-compiler-plugin` con anotation processors de Lombok/MapStruct). Cada microservicio es un módulo hijo con su propio `pom.xml`, su propio `compose.yaml` si necesita infraestructura local, y su propio paquete base `com.javacadabra.tienda.<contexto>`.

Para añadir un microservicio nuevo: crear el módulo, declararlo en `<modules>` del `pom.xml` raíz, y seguir la misma convención de capas de la tabla anterior.

Microservicio actual: `servicio-catalogo` (Catálogo/Productos, capítulo 1).

## Comandos

Build tool: Maven, vía wrapper (no requiere instalación local de Maven).

```bash
# Compilar todo el reactor
./mvnw clean package

# Ejecutar todos los tests del reactor
./mvnw test

# Ejecutar los tests de un solo módulo
./mvnw -pl servicio-catalogo test

# Ejecutar una clase de test concreta de un módulo
./mvnw -pl servicio-catalogo test -Dtest=ProductoTest

# Levantar un microservicio en local (arranca su docker-compose automáticamente)
./mvnw -pl servicio-catalogo -am spring-boot:run
```

## Notas históricas

- El artifactId raíz mantiene el guion (`arquitectura-microservicios`) porque ya no determina el nombre del paquete Java: cada módulo tiene su propio paquete base (`com.javacadabra.tienda.<contexto>`), independiente del artifactId.
- Los campos vacíos `name`/`description`/`url`/`licenses`/`developers`/`scm` del `pom.xml` raíz son intencionados: evitan heredar esos valores del `spring-boot-starter-parent` (ver `HELP.md`).
