# Capítulo 10 — Disciplina de tests y arquitectura: ArchUnit, jMolecules, Instancio y DataFaker

Décimo capítulo del tutorial "De cero a pro en arquitectura de microservicios con Spring Boot" (ver el índice completo de capítulos en la rama `main`). Parte directamente de `capitulo-09-test-capa-web`.

## Índice

1. [Motivación: automatizar una disciplina que hasta ahora era manual](#1-motivación-automatizar-una-disciplina-que-hasta-ahora-era-manual)
2. [ArchUnit: las reglas de capas, ahora como test ejecutable](#2-archunit-las-reglas-de-capas-ahora-como-test-ejecutable)
3. [jMolecules: vocabulario DDD explícito en el dominio](#3-jmolecules-vocabulario-ddd-explícito-en-el-dominio)
4. [jMolecules + ArchUnit: reglas DDD automáticas](#4-jmolecules--archunit-reglas-ddd-automáticas)
5. [jMolecules: arquitectura hexagonal como vocabulario verificable](#5-jmolecules-arquitectura-hexagonal-como-vocabulario-verificable)
6. [Instancio: rellenar objetos sin invariantes](#6-instancio-rellenar-objetos-sin-invariantes)
7. [DataFaker: datos realistas para los Objetos de Valor que sí validan](#7-datafaker-datos-realistas-para-los-objetos-de-valor-que-sí-validan)
8. [Cómo probarlo](#8-cómo-probarlo)
9. [Registro de archivos del capítulo](#9-registro-de-archivos-del-capítulo)
10. [Referencias](#10-referencias)

---

## 1. Motivación: automatizar una disciplina que hasta ahora era manual

Desde el capítulo 1, cada microservicio de este tutorial sigue la misma convención de capas (**dominio**, **aplicación**, **infraestructura**), y el mismo patrón para los **Agregados** (Aggregate): constructor privado, factorías estáticas `crear`/`reconstruir`, invariantes validadas en el propio constructor. Hasta ahora esa disciplina se ha mantenido por revisión manual — el `README.md` de cada capítulo la explica, pero nada impide que un commit futuro rompa una de esas reglas sin que nadie lo note hasta una revisión de código.

Con `servicio-catalogo` y `servicio-pedidos` ya construidos, ese riesgo deja de ser hipotético: cualquier regla de capas o de modelado que se documenta en un solo sitio y se aplica de memoria en el otro es una regla que, tarde o temprano, diverge entre los dos. Este capítulo cierra ese hueco con dos herramientas complementarias:

- **ArchUnit**, para expresar como test ejecutable las reglas de capas que hasta ahora solo vivían en la documentación (p. ej. "el dominio no depende de la infraestructura").
- **jMolecules**, para anotar el modelo de dominio con vocabulario DDD explícito (`@AggregateRoot`, `@ValueObject`, `@Entity`...) y, sobre esas anotaciones, reglas de ArchUnit ya escritas que verifican invariantes estructurales que las reglas de capas por paquete no capturan — p. ej. que un Agregado nunca referencia directamente a otro Agregado.

Se aplican ambas a los dos microservicios, no solo a uno: el valor de automatizar la disciplina crece con el número de servicios y agregados a mantener consistentes, y verlo funcionar sobre dos modelos de dominio distintos (`Producto`/`Categoria` en catálogo, `Pedido`/`LineaPedido` en pedidos) es lo que demuestra que la regla generaliza y no es una coincidencia del primer caso.

Como complemento, dos herramientas de generación de datos de prueba —**Instancio** y **DataFaker**— sustituyen el boilerplate de construir objetos de prueba a mano que ya empezaba a notarse en los tests de capa web del capítulo anterior.

## 2. ArchUnit: las reglas de capas, ahora como test ejecutable

La regla más básica de la Arquitectura Hexagonal de este proyecto es que el paquete `dominio` no conoce nada de `aplicacion` ni de `infraestructura`, y que `aplicacion` tampoco depende de `infraestructura` (las flechas de dependencia solo entran hacia el dominio a través de los puertos, nunca al revés). [ArchUnit](https://www.archunit.org/) permite expresar exactamente esa regla como un test JUnit 5 más, que falla si alguien la rompe:

```java
@AnalyzeClasses(packages = "com.javacadabra.tienda.catalogo", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquitecturaHexagonalTest {

	@ArchTest
	static final ArchRule elDominioNoDependeDeOtrasCapas = noClasses()
			.that().resideInAPackage("..dominio..")
			.should().dependOnClassesThat().resideInAnyPackage("..aplicacion..", "..infraestructura..");

	@ArchTest
	static final ArchRule laAplicacionNoDependeDeInfraestructura = noClasses()
			.that().resideInAPackage("..aplicacion..")
			.should().dependOnClassesThat().resideInAPackage("..infraestructura..");

	// continúa en la sección 4
}
```

`@AnalyzeClasses` le dice a ArchUnit qué paquete escanear por reflexión de bytecode (no hace falta instanciar nada), y `@ArchTest` marca cada campo `ArchRule` como un test independiente que el motor de JUnit 5 de ArchUnit (`archunit-junit5`) descubre y ejecuta igual que cualquier `@Test`.

> **¿Por qué `importOptions = ImportOption.DoNotIncludeTests.class`?**
>
> Sin ese filtro, ArchUnit escanea también las propias clases de test del módulo — y los tests de integración de este mismo capítulo (los que ejercitan `ProductoRepositorioAdaptador` o `ProductoController` directamente) violarían más adelante la regla hexagonal de la [sección 5](#5-jmolecules-arquitectura-hexagonal-como-vocabulario-verificable), que sí prohíbe llamar a un adaptador desde fuera de la propia capa de adaptadores. Ese tipo de llamada directa es exactamente lo que un test de integración necesita hacer para funcionar, así que no es una violación real de la arquitectura de producción — es ruido del propio test evaluándose a sí mismo. `DoNotIncludeTests` limita el escaneo al código de `src/main`, que es donde estas reglas tienen sentido.

## 3. jMolecules: vocabulario DDD explícito en el dominio

[jMolecules](https://github.com/xmolecules/jmolecules) es una librería de anotaciones e interfaces — sin ningún comportamiento en tiempo de ejecución — que expresa conceptos DDD directamente en el código: `@AggregateRoot`, `@Entity`, `@ValueObject`, `@Identity`, `@Repository`. No sustituye nada de lo que ya existía (el constructor privado, las factorías `crear`/`reconstruir`, `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` de la convención de Lombok en agregados): añade una capa de metadatos sobre ese mismo código, legible tanto por otro desarrollador como por herramientas como ArchUnit.

> **¿No es esto justo el tipo de dependencia de framework que `dominio` tiene prohibida?**
>
> No en el sentido en el que se prohíbe. La regla es que el dominio no puede depender de nada que le imponga comportamiento en tiempo de ejecución — un framework de persistencia, un contenedor de inyección de dependencias, un servidor web. jMolecules no aporta ninguna de esas cosas: sus anotaciones son puro metadato (`RetentionPolicy.RUNTIME` solo para que herramientas como ArchUnit puedan leerlas por reflexión), sin una sola línea de lógica que se ejecute contra el dominio. Es la misma distinción que ya aplica a Lombok en la convención de agregados: generar código en tiempo de compilación no es lo mismo que acoplar el dominio a un framework.

Aplicado a `Producto` (`servicio-catalogo`):

```java
@AggregateRoot
@Getter
@Accessors(fluent = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Producto {

	@Identity
	@EqualsAndHashCode.Include
	private final ProductoId id;
	// ...
}
```

Y a los Objetos de Valor (Value Object), que ya eran `record` desde el capítulo 1 — la anotación es el único cambio:

```java
@ValueObject
public record ProductoId(String valor) {
	// ...
}
```

La misma anotación se repite en `Categoria` (catálogo) y en `Pedido` (pedidos) como `@AggregateRoot`, y en `CategoriaId`/`Precio`/`PedidoId`/`ClienteId`/`Cantidad`/`ProductoId` como `@ValueObject`.

> **¿Por qué `LineaPedido` no lleva `@Entity`?**
>
> `LineaPedido` es una entidad interna al agregado — sin repositorio ni ciclo de vida propios — pero nunca tuvo un campo de identidad: no hay ningún `LineaPedidoId`, y dos líneas se distinguen únicamente por su posición dentro de la lista de `Pedido`. jMolecules exige que toda clase anotada `@Entity` declare un miembro anotado `@Identity` (lo verifica la regla `annotatedEntitiesAndAggregatesNeedToHaveAnIdentifier()` de la [sección 4](#4-jmolecules--archunit-reglas-ddd-automáticas)) — y forzar una identidad que el modelo nunca necesitó solo para poder poner la anotación habría sido maquillar el código para complacer a la herramienta, no documentarlo con más precisión. Se deja `LineaPedido` sin anotación de jMolecules: sigue siendo una entidad interna en ese sentido más amplio, simplemente no en el sentido más estricto que jMolecules exige para su propia anotación `@Entity`.

Por el mismo motivo tampoco se usan `@Service` ni `@Factory` de jMolecules sobre `aplicacion.servicio.*Servicio`: esas anotaciones describen un Servicio de Dominio (lógica sin dueño natural en ningún Agregado) y una Fábrica dedicada respectivamente, y las clases de este proyecto en `aplicacion.servicio` son casos de uso que orquestan Agregados y puertos de salida — un rol más cercano a lo que jMolecules llama capa de aplicación que a un Servicio de Dominio. Forzar la anotación habría sido igual de artificial que inventarle una identidad a `LineaPedido`.

## 4. jMolecules + ArchUnit: reglas DDD automáticas

Las anotaciones de la sección anterior no son solo documentación: el módulo `jmolecules-archunit` trae reglas de ArchUnit ya escritas que las leen por reflexión y verifican invariantes estructurales de DDD que ninguna regla de capas por paquete captura. `JMoleculesDddRules.all()` combina cuatro de esas reglas en una sola:

```java
@ArchTest
static final ArchRule reglasDdd = JMoleculesDddRules.all();
```

1. Toda `@Entity` declarada dentro de un `@AggregateRoot` pertenece a ese mismo Agregado (no se referencia una entidad de otro Agregado por error).
2. Un `@AggregateRoot` nunca referencia directamente a otro `@AggregateRoot` — solo a través de su identificador o de una `Association<T, ID>` propia de jMolecules. `Producto.categoriaId` cumple esto de forma natural: es un `CategoriaId`, no una referencia directa a `Categoria`.
3. Toda clase anotada `@AggregateRoot` o `@Entity` declara un miembro `@Identity` (la regla que `LineaPedido` no cumpliría si se hubiera anotado como `@Entity`, ver la nota de la [sección 3](#3-jmolecules-vocabulario-ddd-explícito-en-el-dominio)).
4. Un `@ValueObject` no referencia a nada identificable (otro Agregado o Entidad) — solo tiene sentido si es inmutable y se compara por valor.

Las cuatro pasan limpias sobre `Producto`/`Categoria` y sobre `Pedido`/`LineaPedido` sin cambiar ni una línea del modelo — son exactamente las invariantes que el diseño ya respetaba desde que se escribió, solo que hasta ahora nadie las comprobaba en cada build.

## 5. jMolecules: arquitectura hexagonal como vocabulario verificable

Las mismas anotaciones DDD tienen un equivalente para Arquitectura Hexagonal: `@PrimaryPort`/`@SecondaryPort` sobre los **Puertos de entrada** (Inbound Port) y **Puertos de salida** (Outbound Port), y `@PrimaryAdapter`/`@SecondaryAdapter` sobre los adaptadores que los implementan. El encaje con la convención de paquetes de este monorepo es casi literal — `aplicacion.puerto.entrada` ya se llama así porque es un Puerto de entrada:

```java
@Repository
@SecondaryPort
public interface ProductoRepositorioPuertoSalida {
	// ...
}
```

```java
@PrimaryAdapter
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController implements ProductoApiDoc {
	// ...
}
```

(`@Repository` aquí es la anotación DDD de jMolecules, no el estereotipo de Spring — este proyecto nunca ha anotado sus adaptadores de persistencia con el `@Repository` de Spring, usa `@Component` desde el capítulo 1, así que no hay colisión de nombres en ningún archivo real.)

Con esas cuatro anotaciones repartidas por los puertos y adaptadores de cada microservicio, `JMoleculesArchitectureRules.ensureHexagonal()` verifica automáticamente la regla que hasta ahora nadie comprobaba: un adaptador primario (`ProductoController`) solo puede depender de puertos primarios, nunca de un adaptador secundario directamente; un adaptador secundario (`ProductoRepositorioAdaptador`) solo se accede desde dentro de la propia capa de adaptadores, nunca desde fuera:

```java
@ArchTest
static final ArchRule reglaHexagonal = JMoleculesArchitectureRules.ensureHexagonal();
```

> **Una versión "más reciente" que en realidad es un release residual de 2022**
>
> Al añadir la dependencia `org.jmolecules.integrations:jmolecules-archunit`, Maven Central listaba `1.6.0` como la versión `<latest>`/`<release>` — más alta, numéricamente, que cualquier `0.x`. Resultó ser un release publicado el 21 de junio de 2022, un día antes de `0.11.0`: un salto de numeración aislado que quedó huérfano en la serie `0.x` en vez de continuarla, y que Maven Central sigue anunciando como "la última" porque solo compara números de versión, no fechas de publicación. Compilada contra ArchUnit 0.23.1 (2022), esa `1.6.0` falla con `NoSuchMethodError`/`AbstractMethodError` en tiempo de ejecución contra el ArchUnit 1.4.1 moderno que usa el resto de este capítulo — la API pública de ArchUnit cambió de forma incompatible entre su serie `0.x` y `1.x`. La versión realmente activa y mantenida es `0.28.0` (junio 2025), que sí declara ArchUnit `1.4.1` como dependencia. Verificado con `curl` contra `maven-metadata.xml` y comparando fechas de publicación reales en `search.maven.org`, no dando por buena la etiqueta `<latest>` sin más.

> **Un conflicto de versión adicional: `archunit` en dos números distintos a la vez**
>
> Incluso con `jmolecules-archunit:0.28.0`, `mvn dependency:tree -Dverbose` mostraba `com.tngtech.archunit:archunit:0.23.1` ganando sobre `1.4.1`: Maven resuelve conflictos de versión por cercanía en el árbol de dependencias, y `jmolecules-archunit` declara `archunit:0.23.1` como dependencia directa (un salto menos de profundidad que el `archunit:1.4.1` que llega transitivamente a través de `archunit-junit5`). La solución es forzar la versión en el `dependencyManagement` del `pom.xml` raíz — eso gana siempre, sin importar la profundidad del árbol:
> ```xml
> <dependency>
>     <groupId>com.tngtech.archunit</groupId>
>     <artifactId>archunit</artifactId>
>     <version>${archunit.version}</version>
> </dependency>
> ```

## 6. Instancio: rellenar objetos sin invariantes

`ProductoEntidad` (la entidad de persistencia Neo4j de `servicio-catalogo`, capítulo 1) no tenía, hasta este capítulo, ningún test dedicado a su mapper — `ProductoEntidadMapperTest` es nuevo. Es exactamente el tipo de objeto que [Instancio](https://www.instancio.org/) está pensado para rellenar: un `@NoArgsConstructor`/`@AllArgsConstructor` de Lombok sin ninguna validación propia, a diferencia del Agregado `Producto` o de `ProductoId`/`Precio`, que sí tienen invariantes en el constructor y por eso quedan fuera de este uso.

```java
ProductoEntidad entidad = Instancio.of(ProductoEntidad.class)
		.generate(field(ProductoEntidad::getId), gen -> gen.text().uuid())
		.generate(field(ProductoEntidad::getPrecio), gen -> gen.math().bigDecimal().min(BigDecimal.ZERO))
		.generate(field(CategoriaEntidad::getId), gen -> gen.text().uuid())
		.create();

Producto producto = mapper.aDominio(entidad);
```

> **Si `ProductoEntidad` no tiene invariantes, ¿por qué hacen falta tres `generate(...)`?**
>
> Porque "sin invariantes propias" no es lo mismo que "sus valores nunca se validan en ningún sitio". `ProductoEntidadMapper.aDominio(entidad)` pasa `entidad.getId()` al constructor de `ProductoId`, que sí exige un UUID válido, y `entidad.getPrecio()` al de `Precio`, que exige un valor no negativo — la invariante existe, solo que vive en el **Objeto de Valor** (Value Object) de destino, no en la propia entidad de origen. Sin estos tres generadores dirigidos, Instancio genera por defecto una cadena alfanumérica arbitraria para cualquier campo `String` (algo como `"NHXJYDOW"`, verificado ejecutando el test sin ellos) y un `BigDecimal` que puede caer en negativo — y `mapper.aDominio(entidad)` lanza `IllegalArgumentException` antes de llegar a la primera aserción. `field(ProductoEntidad::getId)` selecciona el campo por referencia al método getter (funciona igual con `CategoriaEntidad::getId`, aunque `CategoriaEntidad` sea un campo anidado dentro de `ProductoEntidad`), y `.generate(selector, gen -> ...)` sustituye el generador por defecto de ese campo por uno acotado al formato o rango que el destino necesita.

El segundo test del mismo archivo mapea en la dirección contraria (`Producto` de dominio → `ProductoEntidad`) y no necesita ningún generador dirigido para la `CategoriaEntidad` de destino — `Instancio.create(CategoriaEntidad.class)` a secas es suficiente, porque `aEntidad(...)` no valida nada, solo enlaza la instancia recibida tal cual. El contraste entre los dos tests es la lección completa: la necesidad de un generador dirigido no depende de la clase que Instancio rellena, sino de si el valor generado acaba pasando por una validación aguas abajo.

## 7. DataFaker: datos realistas para los Objetos de Valor que sí validan

Para el lado del `Producto` de dominio en ese mismo segundo test, [DataFaker](https://www.datafaker.net/) genera los valores que entran por `Producto.crear(...)` — el sucesor activo de Java Faker, sin releases desde 2020:

```java
Producto producto = Producto.crear(
		faker.commerce().productName(),
		faker.lorem().sentence(),
		Precio.de(new BigDecimal(faker.commerce().price(1, 500))),
		CategoriaId.generar());
```

`faker.commerce().productName()` compone un nombre de producto con adjetivo + material + artículo (p. ej. "Sleek Cotton Chair"), y `faker.commerce().price(1, 500)` devuelve directamente una cadena con dos decimales dentro del rango — ya en el formato que `BigDecimal` y, aguas abajo, la invariante "no negativo" de `Precio` esperan. A diferencia de Instancio, aquí no hace falta ningún generador dirigido: DataFaker ya produce valores realistas y válidos por defecto, así que la elección entre una y otra herramienta depende de qué se necesita rellenar — datos de dominio con significado (nombre de producto, precio de catálogo) van con DataFaker; estructuras de datos sin ese significado (una entidad de persistencia cualquiera) van con Instancio.

## 8. Cómo probarlo

```bash
# Todos los tests de arquitectura + los de datos de prueba de este capítulo, en catálogo
./mvnw -pl servicio-catalogo test -Dtest=ArquitecturaHexagonalTest,ProductoEntidadMapperTest

# El equivalente en pedidos
./mvnw -pl servicio-pedidos test -Dtest=ArquitecturaHexagonalTest

# Todo el reactor
./mvnw test
```

## 9. Registro de archivos del capítulo

🌱 Creado · ✏️ Actualizado · 🗑️ Eliminado

### Build y configuración

|     | Archivo | Descripción funcional | Descripción del cambio |
|:---:|---|---|---|
| ✏️ | [`pom.xml`](pom.xml) | Parent multi-módulo | Añade `archunit-junit5`, `jmolecules-bom`, `jmolecules-archunit`, `instancio-junit` y `datafaker` al `dependencyManagement`, con la versión de `archunit` forzada explícitamente para evitar el conflicto de mediación descrito en la sección 5 |
| ✏️ | [`servicio-catalogo/pom.xml`](servicio-catalogo/pom.xml) | Build de `servicio-catalogo` | Añade `jmolecules-ddd`/`jmolecules-hexagonal-architecture` (compile) y `archunit-junit5`/`jmolecules-archunit`/`instancio-junit`/`datafaker` (test) |
| ✏️ | [`servicio-pedidos/pom.xml`](servicio-pedidos/pom.xml) | Build de `servicio-pedidos` | Mismas dependencias que `servicio-catalogo/pom.xml` |

### Dominio (servicio-catalogo)

|     | Archivo | Descripción funcional | Descripción del cambio |
|:---:|---|---|---|
| ✏️ | [`servicio-catalogo/.../agregado/Producto.java`](servicio-catalogo/src/main/java/com/javacadabra/tienda/catalogo/dominio/modelo/agregado/Producto.java) | Agregado raíz de producto | `@AggregateRoot` en la clase, `@Identity` en el campo `id` |
| ✏️ | [`servicio-catalogo/.../agregado/Categoria.java`](servicio-catalogo/src/main/java/com/javacadabra/tienda/catalogo/dominio/modelo/agregado/Categoria.java) | Agregado raíz de categoría | `@AggregateRoot` en la clase, `@Identity` en el campo `id` |
| ✏️ | [`servicio-catalogo/.../objetovalor/ProductoId.java`](servicio-catalogo/src/main/java/com/javacadabra/tienda/catalogo/dominio/modelo/objetovalor/ProductoId.java) | Objeto de Valor, identificador de producto | `@ValueObject` |
| ✏️ | [`servicio-catalogo/.../objetovalor/CategoriaId.java`](servicio-catalogo/src/main/java/com/javacadabra/tienda/catalogo/dominio/modelo/objetovalor/CategoriaId.java) | Objeto de Valor, identificador de categoría | `@ValueObject` |
| ✏️ | [`servicio-catalogo/.../objetovalor/Precio.java`](servicio-catalogo/src/main/java/com/javacadabra/tienda/catalogo/dominio/modelo/objetovalor/Precio.java) | Objeto de Valor, precio de producto | `@ValueObject` |

### Dominio (servicio-pedidos)

|     | Archivo | Descripción funcional | Descripción del cambio |
|:---:|---|---|---|
| ✏️ | [`servicio-pedidos/.../agregado/Pedido.java`](servicio-pedidos/src/main/java/com/javacadabra/tienda/pedidos/dominio/modelo/agregado/Pedido.java) | Agregado raíz de pedido | `@AggregateRoot` en la clase, `@Identity` en el campo `id` |
| ✏️ | [`servicio-pedidos/.../objetovalor/PedidoId.java`](servicio-pedidos/src/main/java/com/javacadabra/tienda/pedidos/dominio/modelo/objetovalor/PedidoId.java) | Objeto de Valor, identificador de pedido | `@ValueObject` |
| ✏️ | [`servicio-pedidos/.../objetovalor/ClienteId.java`](servicio-pedidos/src/main/java/com/javacadabra/tienda/pedidos/dominio/modelo/objetovalor/ClienteId.java) | Objeto de Valor, identificador de cliente | `@ValueObject` |
| ✏️ | [`servicio-pedidos/.../objetovalor/Cantidad.java`](servicio-pedidos/src/main/java/com/javacadabra/tienda/pedidos/dominio/modelo/objetovalor/Cantidad.java) | Objeto de Valor, cantidad de una línea de pedido | `@ValueObject` |
| ✏️ | [`servicio-pedidos/.../objetovalor/ProductoId.java`](servicio-pedidos/src/main/java/com/javacadabra/tienda/pedidos/dominio/modelo/objetovalor/ProductoId.java) | Objeto de Valor, identificador de producto (copia local del Contexto Delimitado de pedidos) | `@ValueObject` |
| ✏️ | [`servicio-pedidos/.../objetovalor/Precio.java`](servicio-pedidos/src/main/java/com/javacadabra/tienda/pedidos/dominio/modelo/objetovalor/Precio.java) | Objeto de Valor, precio unitario de una línea de pedido | `@ValueObject` |

### Aplicación e infraestructura (servicio-catalogo)

|     | Archivo | Descripción funcional | Descripción del cambio |
|:---:|---|---|---|
| ✏️ | [`servicio-catalogo/.../salida/ProductoRepositorioPuertoSalida.java`](servicio-catalogo/src/main/java/com/javacadabra/tienda/catalogo/aplicacion/puerto/salida/ProductoRepositorioPuertoSalida.java) | Puerto de salida de persistencia de producto | `@Repository` (jMolecules) + `@SecondaryPort` |
| ✏️ | [`servicio-catalogo/.../salida/CategoriaRepositorioPuertoSalida.java`](servicio-catalogo/src/main/java/com/javacadabra/tienda/catalogo/aplicacion/puerto/salida/CategoriaRepositorioPuertoSalida.java) | Puerto de salida de persistencia de categoría | `@Repository` (jMolecules) + `@SecondaryPort` |
| ✏️ | [`servicio-catalogo/.../entrada/CrearProductoPuertoEntrada.java`](servicio-catalogo/src/main/java/com/javacadabra/tienda/catalogo/aplicacion/puerto/entrada/CrearProductoPuertoEntrada.java) | Puerto de entrada, caso de uso crear producto | `@PrimaryPort` |
| ✏️ | [`servicio-catalogo/.../entrada/BuscarProductoPuertoEntrada.java`](servicio-catalogo/src/main/java/com/javacadabra/tienda/catalogo/aplicacion/puerto/entrada/BuscarProductoPuertoEntrada.java) | Puerto de entrada, caso de uso buscar producto por id | `@PrimaryPort` |
| ✏️ | [`servicio-catalogo/.../entrada/BuscarProductosPorCategoriaPuertoEntrada.java`](servicio-catalogo/src/main/java/com/javacadabra/tienda/catalogo/aplicacion/puerto/entrada/BuscarProductosPorCategoriaPuertoEntrada.java) | Puerto de entrada, caso de uso buscar productos por categoría | `@PrimaryPort` |
| ✏️ | [`servicio-catalogo/.../entrada/RecomendarProductoPuertoEntrada.java`](servicio-catalogo/src/main/java/com/javacadabra/tienda/catalogo/aplicacion/puerto/entrada/RecomendarProductoPuertoEntrada.java) | Puerto de entrada, caso de uso recomendar producto | `@PrimaryPort` |
| ✏️ | [`servicio-catalogo/.../entrada/BuscarProductosRecomendadosPuertoEntrada.java`](servicio-catalogo/src/main/java/com/javacadabra/tienda/catalogo/aplicacion/puerto/entrada/BuscarProductosRecomendadosPuertoEntrada.java) | Puerto de entrada, caso de uso buscar productos recomendados | `@PrimaryPort` |
| ✏️ | [`servicio-catalogo/.../entrada/CrearCategoriaPuertoEntrada.java`](servicio-catalogo/src/main/java/com/javacadabra/tienda/catalogo/aplicacion/puerto/entrada/CrearCategoriaPuertoEntrada.java) | Puerto de entrada, caso de uso crear categoría | `@PrimaryPort` |
| ✏️ | [`servicio-catalogo/.../entrada/BuscarCategoriaPuertoEntrada.java`](servicio-catalogo/src/main/java/com/javacadabra/tienda/catalogo/aplicacion/puerto/entrada/BuscarCategoriaPuertoEntrada.java) | Puerto de entrada, caso de uso buscar categoría por id | `@PrimaryPort` |
| ✏️ | [`servicio-catalogo/.../rest/ProductoController.java`](servicio-catalogo/src/main/java/com/javacadabra/tienda/catalogo/infraestructura/adaptador/entrada/rest/ProductoController.java) | Adaptador de entrada REST de producto | `@PrimaryAdapter` |
| ✏️ | [`servicio-catalogo/.../rest/CategoriaController.java`](servicio-catalogo/src/main/java/com/javacadabra/tienda/catalogo/infraestructura/adaptador/entrada/rest/CategoriaController.java) | Adaptador de entrada REST de categoría | `@PrimaryAdapter` |
| ✏️ | [`servicio-catalogo/.../adaptador/ProductoRepositorioAdaptador.java`](servicio-catalogo/src/main/java/com/javacadabra/tienda/catalogo/infraestructura/adaptador/salida/persistencia/adaptador/ProductoRepositorioAdaptador.java) | Adaptador de salida de persistencia de producto (Neo4j) | `@SecondaryAdapter` |
| ✏️ | [`servicio-catalogo/.../adaptador/CategoriaRepositorioAdaptador.java`](servicio-catalogo/src/main/java/com/javacadabra/tienda/catalogo/infraestructura/adaptador/salida/persistencia/adaptador/CategoriaRepositorioAdaptador.java) | Adaptador de salida de persistencia de categoría (Neo4j) | `@SecondaryAdapter` |

### Aplicación e infraestructura (servicio-pedidos)

|     | Archivo | Descripción funcional | Descripción del cambio |
|:---:|---|---|---|
| ✏️ | [`servicio-pedidos/.../salida/PedidoRepositorioPuertoSalida.java`](servicio-pedidos/src/main/java/com/javacadabra/tienda/pedidos/aplicacion/puerto/salida/PedidoRepositorioPuertoSalida.java) | Puerto de salida de persistencia de pedido | `@Repository` (jMolecules) + `@SecondaryPort` |
| ✏️ | [`servicio-pedidos/.../salida/CatalogoPuertoSalida.java`](servicio-pedidos/src/main/java/com/javacadabra/tienda/pedidos/aplicacion/puerto/salida/CatalogoPuertoSalida.java) | Puerto de salida, Capa Anticorrupción hacia `servicio-catalogo` | `@SecondaryPort` (sin `@Repository`: no es persistencia, es un puerto hacia otro Contexto Delimitado) |
| ✏️ | [`servicio-pedidos/.../entrada/CrearPedidoPuertoEntrada.java`](servicio-pedidos/src/main/java/com/javacadabra/tienda/pedidos/aplicacion/puerto/entrada/CrearPedidoPuertoEntrada.java) | Puerto de entrada, caso de uso crear pedido | `@PrimaryPort` |
| ✏️ | [`servicio-pedidos/.../rest/PedidoController.java`](servicio-pedidos/src/main/java/com/javacadabra/tienda/pedidos/infraestructura/adaptador/entrada/rest/PedidoController.java) | Adaptador de entrada REST de pedido | `@PrimaryAdapter` |
| ✏️ | [`servicio-pedidos/.../adaptador/PedidoRepositorioAdaptador.java`](servicio-pedidos/src/main/java/com/javacadabra/tienda/pedidos/infraestructura/adaptador/salida/persistencia/adaptador/PedidoRepositorioAdaptador.java) | Adaptador de salida de persistencia de pedido (JPA) | `@SecondaryAdapter` |
| ✏️ | [`servicio-pedidos/.../adaptador/CatalogoAdaptador.java`](servicio-pedidos/src/main/java/com/javacadabra/tienda/pedidos/infraestructura/adaptador/salida/http/adaptador/CatalogoAdaptador.java) | Adaptador de salida HTTP hacia `servicio-catalogo` | `@SecondaryAdapter` |

### Tests

|     | Archivo | Descripción funcional | Descripción del cambio |
|:---:|---|---|---|
| 🌱 | [`servicio-catalogo/.../arquitectura/ArquitecturaHexagonalTest.java`](servicio-catalogo/src/test/java/com/javacadabra/tienda/catalogo/arquitectura/ArquitecturaHexagonalTest.java) | Reglas de capas (ArchUnit a mano) + reglas DDD/hexagonal (jMolecules) sobre `servicio-catalogo` | --- |
| 🌱 | [`servicio-pedidos/.../arquitectura/ArquitecturaHexagonalTest.java`](servicio-pedidos/src/test/java/com/javacadabra/tienda/pedidos/arquitectura/ArquitecturaHexagonalTest.java) | Mismas reglas sobre `servicio-pedidos` | --- |
| 🌱 | [`servicio-catalogo/.../mapper/ProductoEntidadMapperTest.java`](servicio-catalogo/src/test/java/com/javacadabra/tienda/catalogo/infraestructura/adaptador/salida/persistencia/mapper/ProductoEntidadMapperTest.java) | Test del mapper entidad↔dominio de producto, con Instancio y DataFaker | --- |

## 10. Referencias

- [ArchUnit — User Guide](https://www.archunit.org/userguide/html/000_Index.html)
- [jMolecules — GitHub](https://github.com/xmolecules/jmolecules)
- [jMolecules Integrations — ArchUnit rules](https://github.com/xmolecules/jmolecules-integrations/tree/main/jmolecules-archunit)
- [Instancio — User Guide](https://www.instancio.org/user-guide/)
- [Datafaker — Documentation](https://www.datafaker.net/documentation/getting-started/)
