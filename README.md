# Capítulo 1 — Fundamentos DDD + Arquitectura Hexagonal

Primer capítulo del tutorial "De cero a pro en arquitectura de microservicios con Spring Boot" (ver el índice completo de capítulos en la rama `main`).

## Índice

1. [Introducción](#1-introducción)
2. [Domain-Driven Design: los conceptos que usamos](#2-domain-driven-design-los-conceptos-que-usamos)
3. [Arquitectura Hexagonal (Ports & Adapters)](#3-arquitectura-hexagonal-ports--adapters)
4. [El monorepo Maven multi-módulo](#4-el-monorepo-maven-multi-módulo)
5. [El dominio: el agregado `Producto`](#5-el-dominio-el-agregado-producto)
6. [La aplicación: puertos y casos de uso](#6-la-aplicación-puertos-y-casos-de-uso)
7. [La infraestructura de entrada: el adaptador REST](#7-la-infraestructura-de-entrada-el-adaptador-rest)
8. [Acceso a la base de datos con Spring Data Neo4j](#8-acceso-a-la-base-de-datos-con-spring-data-neo4j)
9. [Testing: de los tests unitarios a Testcontainers](#9-testing-de-los-tests-unitarios-a-testcontainers)
10. [Diagramas](#10-diagramas)
11. [Cómo probarlo de extremo a extremo](#11-cómo-probarlo-de-extremo-a-extremo)
12. [Qué se deja para el capítulo 2](#12-qué-se-deja-para-el-capítulo-2)
13. [Registro de archivos del capítulo](#13-registro-de-archivos-del-capítulo)
14. [Referencias](#14-referencias)

---

## 1. Introducción

Este capítulo construye el primer microservicio de la tienda online: **`servicio-catalogo`** (gestión de productos). No es solo código que "funciona": es la referencia arquitectónica que copiaremos para el resto de microservicios del tutorial, así que cada decisión de diseño está explicada, no solo aplicada.

Al terminar este capítulo entenderás:

- Por qué el dominio se modela con **agregados** y **objetos de valor**, y qué problema resuelve el **lenguaje ubicuo**.
- Cómo se traduce la **Arquitectura Hexagonal** (puertos y adaptadores) a paquetes y clases Java concretas.
- Cómo **Spring Data Neo4j** conecta ese dominio con una base de datos de grafos real.
- Cómo **Testcontainers** nos permite testear contra un Neo4j real (no un mock) sin instalar nada a mano.

El repositorio pasa a ser un **monorepo multi-módulo Maven**: el `pom.xml` raíz es ahora el parent (`packaging=pom`) y `servicio-catalogo` es el primer módulo hijo. Cada microservicio futuro será un módulo nuevo siguiendo el mismo patrón.

---

## 2. Domain-Driven Design: los conceptos que usamos

DDD (Domain-Driven Design) es, en esencia, una forma de asegurarse de que el código dice lo mismo que dicen los expertos del negocio. Este capítulo usa tres piezas de su caja de herramientas:

### Lenguaje ubicuo (Ubiquitous Language)

Si un experto de negocio habla de "el precio de un producto", el código no debería llamarlo `amount` ni `value` dentro de una clase `Item`. Por eso el modelo de dominio de este proyecto está en español: `Producto`, `Precio`, `ProductoId`. No es una cuestión estética — es que el nombre de la clase y el nombre que usaría alguien de negocio deben ser el mismo nombre, para que no haga falta "traducir" mentalmente el código al hablar del negocio (y viceversa).

### Objeto de Valor (Value Object)

Un Objeto de Valor es un dato que se define por su **valor**, no por su identidad — dos Objetos de Valor con el mismo valor son intercambiables. Además, debe ser **inmutable** y **auto-validarse**: no debería poder existir un `Precio` negativo en ningún punto del programa.

```java
// dominio/modelo/objetovalor/Precio.java
public record Precio(BigDecimal valor) {

	public Precio {
		if (valor == null) {
			throw new IllegalArgumentException("El precio no puede ser nulo");
		}
		if (valor.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("El precio no puede ser negativo: " + valor);
		}
	}

	public static Precio de(BigDecimal valor) {
		return new Precio(valor);
	}
}
```

Usamos `record` de Java a propósito: el constructor canónico (el bloque `public Precio { ... }`) se ejecuta en **cada** construcción del objeto, así que es imposible crear un `Precio` inválido — no hay setters, no hay forma de "colar" un valor incorrecto después de construido. `ProductoId` sigue el mismo patrón, pero además garantiza que su valor es siempre un UUID válido:

```java
// dominio/modelo/objetovalor/ProductoId.java
public record ProductoId(String valor) {

	public ProductoId {
		if (valor == null || valor.isBlank()) {
			throw new IllegalArgumentException("El id del producto no puede estar vacío");
		}
		try {
			UUID.fromString(valor);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("El id del producto debe ser un UUID válido: " + valor, e);
		}
	}

	public static ProductoId generar() {
		return new ProductoId(UUID.randomUUID().toString());
	}

	public static ProductoId de(String valor) {
		return new ProductoId(valor);
	}
}
```

Dos factories estáticas, dos intenciones distintas: `generar()` para cuando el dominio crea una identidad nueva, `de(String)` para cuando reconstruimos una identidad que ya existía (por ejemplo, al leerla de la base de datos o de una URL).

### Entidad (interna al agregado)

Antes de llegar al Agregado hace falta un concepto intermedio: la Entidad (Entity). Una Entidad, como un Objeto de Valor, puede tener campos mutables — pero a diferencia de un Objeto de Valor, **se compara por identidad, no por valor**: dos entidades con los mismos atributos pero id distinto son objetos distintos, y una misma entidad sigue siendo "la misma" aunque cambien sus atributos. En esto se parece a un Agregado (que, de hecho, es en sí mismo una Entidad: la que hace de raíz).

La diferencia entre una Entidad interna y el Agregado raíz no es de naturaleza sino de **rol dentro del límite de consistencia**: la raíz es el único punto de entrada — se guarda, se recupera y se referencia desde fuera del agregado como una unidad —, mientras que una Entidad interna vive *dentro* de ese límite, sin repositorio propio ni ciclo de vida independiente: no se puede cargar ni guardar suelta, solo a través de la raíz que la contiene. Siguiendo la convención de este proyecto (tabla de la sección 4 de `CLAUDE.md`), una Entidad interna iría en `dominio.modelo.entidad`, junto a (pero separada de) `dominio.modelo.agregado`.

`Producto` en este capítulo **no tiene ninguna Entidad interna** — solo Objetos de Valor (`Precio`, `ProductoId`) además de sí mismo como raíz. Es una decisión deliberada de alcance, no una omisión: forzar una entidad interna artificial (p. ej. "reseñas" o "variantes de producto") solo para ilustrar el concepto no aportaría nada al caso de uso actual. Aparecerá con un ejemplo de código real en un capítulo futuro, cuando un agregado la necesite de forma natural (candidato: variantes o líneas dentro de un agregado `Pedido`) — ver `CHECKLIST.md`, sección "Arquitectura / DDD".

### Agregado (Aggregate)

Un Agregado es el límite de consistencia de un conjunto de objetos: se guarda y se recupera como una unidad, y solo se accede a él a través de su raíz (aquí, `Producto`). Las invariantes del negocio (las reglas que siempre deben cumplirse) viven dentro del agregado, no en un service externo:

```java
// dominio/modelo/agregado/Producto.java
@Getter
@Accessors(fluent = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Producto {

	@EqualsAndHashCode.Include
	private final ProductoId id;
	private String nombre;
	private String descripcion;
	private Precio precio;
	private final Instant fechaCreacion;

	private Producto(ProductoId id, String nombre, String descripcion, Precio precio, Instant fechaCreacion) {
		this.id = id;
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.precio = precio;
		this.fechaCreacion = fechaCreacion;
	}

	public static Producto crear(String nombre, String descripcion, Precio precio) {
		validarNombre(nombre);
		Objects.requireNonNull(precio, "El precio no puede ser nulo");
		return new Producto(ProductoId.generar(), nombre, descripcion, precio, Instant.now());
	}

	public static Producto reconstruir(ProductoId id, String nombre, String descripcion, Precio precio, Instant fechaCreacion) {
		return new Producto(id, nombre, descripcion, precio, fechaCreacion);
	}

	private static void validarNombre(String nombre) {
		if (nombre == null || nombre.isBlank()) {
			throw new IllegalArgumentException("El nombre del producto no puede estar vacío");
		}
	}
}
```

Cuatro decisiones deliberadas aquí:

1. **Constructor privado + factories estáticas nombradas** (`crear`, `reconstruir`): un agregado nuevo (creado por el negocio, con validación completa) y un agregado reconstruido (leído desde la base de datos, donde ya confiamos en que es válido) son conceptos distintos, aunque produzcan el mismo tipo de objeto. Nombrar la diferencia evita confundirlas. Esto sigue escrito a mano — Lombok no puede generar un constructor que valide invariantes de negocio. Nótese que el constructor privado **en sí mismo** no valida nada (la validación vive en `crear`), así que técnicamente `@AllArgsConstructor(access = AccessLevel.PRIVATE)` sería seguro aquí — se descarta igualmente porque, si en el futuro la validación se traslada al propio constructor (un refactor habitual en DDD), un constructor generado por Lombok no podría alojarla.
2. **`equals`/`hashCode` basados solo en `id`**: dos productos con el mismo id son el mismo producto, aunque su nombre o precio hayan cambiado — es la semántica de identidad de un agregado, distinta de la semántica de valor de un Objeto de Valor. A diferencia del constructor, esto **sí** lo genera Lombok: `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` en la clase + `@EqualsAndHashCode.Include` solo en `id` reproduce exactamente esta semántica, sin comparar por `nombre`/`precio`/`fechaCreacion`. La regla para decidir no es "¿es dominio o infraestructura?", sino "¿la anotación puede saltarse una invariante de negocio?" — un constructor generado sí podría (permitiría construir un `Producto` sin pasar por `validarNombre`); un `equals`/`hashCode` generado no, así que aquí no hay ningún riesgo.
3. **Getters de solo lectura vía `@Getter` + `@Accessors(fluent = true)`**: un getter no puede saltarse ninguna invariante (solo lee un campo), así que aquí Lombok tampoco tiene contrapartida. `@Accessors(fluent = true)` es la pieza clave: sin ella, `@Getter` generaría accesores estilo JavaBean (`getId()`, `getNombre()`...); con ella, genera exactamente `id()`, `nombre()`, `descripcion()`, `precio()`, `fechaCreacion()` — el mismo nombre y firma que los getters manuales que sustituye, sin el prefijo `get`.
4. **Sin getters estilo JavaBean** (`getNombre()`), sino accesores estilo record (`nombre()`): esto es intencional y consistente con los Objetos de Valor (`Precio`, `ProductoId`, que son `record` y ya usan ese estilo). Como `@Accessors(fluent = true)` reproduce exactamente esos nombres, **los mappers no necesitan ningún cambio**: `ProductoMapper.aDTO(...)` y `ProductoEntidadMapper.aEntidad(...)` (sección 8.4) siguen llamando a `producto.id()`, `producto.nombre()`, `producto.precio()` tal cual — antes eran métodos escritos a mano, ahora los genera Lombok, pero la firma que ve el resto del código no cambia. Sigue habiendo una consecuencia práctica en cómo se escriben esos mappers (MapStruct no puede inferir automáticamente accesores sin prefijo `get`/`is`), lo verás en la [sección 8](#8-acceso-a-la-base-de-datos-con-spring-data-neo4j).

---

## 3. Arquitectura Hexagonal (Ports & Adapters)

La idea central de la Arquitectura Hexagonal es una sola regla de dependencia: **el código de negocio no debe saber nada sobre los detalles técnicos que lo rodean** (frameworks web, bases de datos, colas de mensajes...). Todo lo técnico depende del negocio; el negocio no depende de nada técnico.

Esto se traduce en tres capas y una regla de sentido único:

| Capa | Responsabilidad | Depende de |
|---|---|---|
| `dominio` | Agregados, Objetos de Valor, reglas de negocio | Nada (Java puro) |
| `aplicacion` | Casos de uso, orquesta el dominio | Solo de `dominio` |
| `infraestructura` | Detalles técnicos: REST, Neo4j, frameworks | De `aplicacion` y `dominio` |

`aplicacion` no conoce Spring Data Neo4j ni Spring MVC directamente: define **puertos** (interfaces) que expresan lo que necesita, y dos tipos de adaptador los conectan con el mundo real:

- **Puerto de entrada** (inbound port; `...PuertoEntrada`): la forma en que el mundo exterior invoca un caso de uso (use case). Lo implementa un **servicio de aplicación** (application service): `CrearProductoServicio`.
- **Puerto de salida** (outbound port; `...PuertoSalida`): lo que el caso de uso necesita del mundo exterior (aquí, persistencia). Lo implementa un **adaptador de infraestructura** (`ProductoRepositorioAdaptador`).

```
infraestructura/entrada (REST) → aplicacion (puerto entrada → servicio) → dominio
                                              ↓
infraestructura/salida (Neo4j) ← aplicacion (puerto salida) ←────────────┘
```

Fíjate en el sentido de las flechas del puerto de salida: la **llamada** va de `aplicacion` hacia `infraestructura` (el servicio invoca al repositorio), pero la **dependencia de código** (el `implements`) va de `infraestructura` hacia `aplicacion` — el adaptador Neo4j depende de la interfaz que define `aplicacion`, nunca al revés. Esta inversión es la que permite sustituir Neo4j por otra base de datos sin tocar una sola línea de `aplicacion` o `dominio`. El [diagrama de arquitectura hexagonal](#10-diagramas) de este capítulo lo muestra visualmente.

---

## 4. El monorepo Maven multi-módulo

El `pom.xml` raíz ya no compila código: es solo un `pom` que centraliza versiones y configuración compartida:

```xml
<packaging>pom</packaging>
<modules>
    <module>servicio-catalogo</module>
</modules>
```

Lo que vive en el parent y por qué:

- **`dependencyManagement`**: versiones de `spring-cloud-dependencies`, `mapstruct` y `testcontainers-bom`, para que todos los módulos futuros usen la misma versión sin repetirla.
- **`build/pluginManagement`**: la configuración de `maven-compiler-plugin` (con los *annotation processors* de Lombok y MapStruct encadenados) y de `spring-boot-maven-plugin`, para que cada módulo solo tenga que declarar el plugin, sin repetir su configuración.

`servicio-catalogo/pom.xml` hereda de ese parent y declara únicamente lo que le es propio: Spring Web, Spring Data Neo4j, MapStruct y las dependencias de test (incluidas las de Testcontainers).

---

## 5. El dominio: el agregado `Producto`

Ya lo vimos entero en la [sección 2](#2-domain-driven-design-los-conceptos-que-usamos). Lo único que falta es la excepción de dominio, que expresa un caso de negocio (no encontrar un producto) con su propio tipo, en vez de dejar que se propague una excepción técnica:

```java
// dominio/excepcion/ProductoNoEncontradoException.java
public class ProductoNoEncontradoException extends RuntimeException {

	public ProductoNoEncontradoException(String id) {
		super("No se ha encontrado el producto con id: " + id);
	}
}
```

Nota que este paquete (`dominio`) no importa nada de Spring, Neo4j ni `jakarta.*`. Puedes compilarlo como una librería Java aislada — esa es precisamente la garantía que da la Arquitectura Hexagonal.

---

## 6. La aplicación: puertos y casos de uso

Cada caso de uso es **un puerto de entrada + un servicio que lo implementa**, no una única clase con muchos métodos. Esto mantiene cada caso de uso testeable e independiente:

```java
// aplicacion/puerto/entrada/CrearProductoPuertoEntrada.java
public interface CrearProductoPuertoEntrada {
	ProductoDTO crear(CrearProductoDTO dto);
}
```

```java
// aplicacion/servicio/CrearProductoServicio.java
@Service
@RequiredArgsConstructor
public class CrearProductoServicio implements CrearProductoPuertoEntrada {

	private final ProductoRepositorioPuertoSalida productoRepositorioPuertoSalida;
	private final ProductoMapper productoMapper;

	@Override
	public ProductoDTO crear(CrearProductoDTO dto) {
		Producto producto = Producto.crear(dto.nombre(), dto.descripcion(), Precio.de(dto.precio()));
		Producto guardado = productoRepositorioPuertoSalida.guardar(producto);
		return productoMapper.aDTO(guardado);
	}
}
```

El servicio de aplicación **orquesta**: construye el agregado con las reglas del dominio (`Producto.crear(...)`), delega la persistencia en el puerto de salida (que no sabe que existe Neo4j) y convierte el resultado a un DTO para no filtrar el modelo de dominio fuera de la aplicación. Esa conversión la hace `ProductoMapper`, y aquí aparece el primer efecto práctico de que `Producto` no tenga getters JavaBean:

```java
// aplicacion/mapper/ProductoMapper.java
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductoMapper {

	default ProductoDTO aDTO(Producto producto) {
		if (producto == null) {
			return null;
		}
		return new ProductoDTO(
				producto.id().valor(),
				producto.nombre(),
				producto.descripcion(),
				producto.precio().valor());
	}
}
```

MapStruct genera automáticamente el mapeo cuando puede inferir las propiedades por convención de nombres (`getX()`/`isX()`); como nuestro dominio expone `id()` y no `getId()`, se lo indicamos explícitamente con un método `default` en la propia interfaz del mapper. MapStruct sigue generando una clase `ProductoMapperImpl` anotada como `@Component` de Spring, así que se inyecta igual que cualquier otro bean — solo que este método en concreto lo escribimos nosotros en vez de dejar que lo genere.

`BuscarProductoServicio` sigue el mismo patrón y es quien lanza `ProductoNoEncontradoException` cuando el puerto de salida devuelve un `Optional` vacío.

**Sobre `@RequiredArgsConstructor`**: `CrearProductoServicio`, `BuscarProductoServicio`, `ProductoRepositorioAdaptador` y `ProductoController` no tienen más constructor que uno de inyección de dependencias sobre campos `final` — el caso de uso exacto para el que Lombok sigue aportando valor incluso en Java 25 (no hay ningún equivalente en el lenguaje que genere ese constructor por ti). Fíjate en que esto **no** lo usamos en `Producto`: el agregado necesita un constructor privado más factories nombradas que validan invariantes (`crear`, `reconstruir`), algo que Lombok no puede generar — por eso ahí seguimos escribiéndolo a mano. Regla general (válida para `Producto` y para cualquier agregado futuro del proyecto): Lombok para boilerplate técnico repetitivo (inyección de dependencias, entidades de persistencia, getters de solo lectura) y para lo que no pueda saltarse una invariante de negocio (`equals`/`hashCode` por id); nunca para el constructor que valida esas invariantes.

---

## 7. La infraestructura de entrada: el adaptador REST

`ProductoController` es un adaptador: su única responsabilidad es traducir HTTP a llamadas de puertos de entrada, y viceversa.

```java
@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

	private final CrearProductoPuertoEntrada crearProductoPuertoEntrada;
	private final BuscarProductoPuertoEntrada buscarProductoPuertoEntrada;

	@PostMapping
	public ResponseEntity<ProductoDTO> crear(@RequestBody CrearProductoDTO dto) {
		ProductoDTO creado = crearProductoPuertoEntrada.crear(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(creado);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ProductoDTO> buscarPorId(@PathVariable String id) {
		return ResponseEntity.ok(buscarProductoPuertoEntrada.buscarPorId(id));
	}
}
```

El controlador depende de las **interfaces** de los puertos de entrada, no de las clases `CrearProductoServicio`/`BuscarProductoServicio` directamente — Spring resuelve la implementación concreta por inyección. `ControladorErroresGlobal` (un `@RestControllerAdvice`) traduce las excepciones de dominio a códigos HTTP: `ProductoNoEncontradoException` → 404, `IllegalArgumentException` (las invariantes violadas en los Objetos de Valor/agregado) → 400. Así el controlador no necesita ningún `try/catch`.

---

## 8. Acceso a la base de datos con Spring Data Neo4j

Esta es la sección donde el dominio "toca" una base de datos real. Vamos a verla de fuera hacia dentro: primero cómo se configura la conexión, después cómo se modelan los datos, y por último cómo se accede a ellos, tanto desde el código como manualmente.

### 8.1. Cómo se configura la conexión

Según la [documentación oficial de Spring Boot](https://docs.spring.io/spring-boot/4.1.0/reference/data/nosql.html#data.nosql.neo4j), añadir `spring-boot-starter-data-neo4j` habilita el soporte de repositorios y de gestión de transacciones para Neo4j, y Spring Boot auto-configura un bean `Driver` que **por defecto intenta conectar a `localhost:7687`** usando el protocolo Bolt. Se puede reconfigurar con propiedades `spring.neo4j.*`:

```yaml
spring:
  neo4j:
    uri: "bolt://my-server:7687"
    authentication:
      username: "neo4j"
      password: "secret"
```

En este capítulo no fijamos esas propiedades a mano en `application.properties` — y es a propósito. Cuando ejecutas `./mvnw -pl servicio-catalogo spring-boot:run`, `spring-boot-docker-compose` (que ya está en el `pom.xml`) detecta `compose.yaml`, levanta un contenedor Neo4j y **crea automáticamente el bean de conexión** (`Neo4jConnectionDetails`) apuntando al puerto real que Docker asignó — no hace falta escribir ni una propiedad `spring.neo4j.uri`. Es el mismo mecanismo de "service connection" que usamos en los tests con Testcontainers (sección 9.2): en desarrollo lo activa Docker Compose, en tests lo activa Testcontainers.

Según la documentación oficial: *"cuando se incluye el módulo `spring-boot-docker-compose`, Spring Boot busca ficheros `compose.yml`, ejecuta `docker compose up`, crea los beans de conexión para los contenedores soportados y ejecuta `docker compose stop` al apagar la aplicación"* — es decir, ni siquiera tienes que acordarte de pararlo.

### 8.2. Cómo se modela `Producto` como nodo del grafo

`ProductoEntidad` es la representación de persistencia — deliberadamente distinta del agregado `Producto` del dominio (así el dominio no depende de anotaciones de Spring Data):

```java
// infraestructura/adaptador/salida/persistencia/entidad/ProductoEntidad.java
@Node("Producto")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductoEntidad {

	@Id
	private String id;
	private String nombre;
	private String descripcion;
	private BigDecimal precio;
	private Instant fechaCreacion;
}
```

`@Node("Producto")` le dice a Spring Data Neo4j que cada instancia es un nodo con la etiqueta `:Producto` en el grafo. `@Id` marca `id` como su identificador — y como nosotros mismos generamos el `ProductoId` (un UUID) en el dominio antes de persistir, **no** usamos `@GeneratedValue`: el id ya viene fijado por el agregado. A diferencia del agregado `Producto`, esta clase **sí** es un buen sitio para Lombok: es una clase de mapeo pura, con getters JavaBean y sin invariantes que proteger, así que `@Getter`/`@NoArgsConstructor`/`@AllArgsConstructor` sustituyen boilerplate sin esconder ninguna regla de negocio (Spring Data necesita el constructor vacío para instanciar el objeto antes de rellenar sus campos). Si guardas un producto con id `bb8001ab-...`, en el grafo queda literalmente:

```cypher
CREATE (:Producto {
  id: "bb8001ab-8135-4029-b6a8-60dbd6f5d856",
  nombre: "Camiseta",
  descripcion: "100% algodón",
  precio: 19.99,
  fechaCreacion: "2026-07-04T18:47:...Z"
})
```

### 8.3. El repositorio: `Neo4jRepository`

```java
// infraestructura/adaptador/salida/persistencia/repositorio/ProductoRepositorioNeo4j.java
public interface ProductoRepositorioNeo4j extends Neo4jRepository<ProductoEntidad, String> {
}
```

Solo con extender `Neo4jRepository<ProductoEntidad, String>` (entidad + tipo del id) ya tenemos `save(...)`, `findById(...)`, `findAll()`, `deleteById(...)`, etc., implementados por Spring Data sin escribir Cypher a mano. Como dice la documentación oficial, *"Spring Data incluye soporte de repositorios para Neo4j, compartiendo la infraestructura común con otros módulos de Spring Data"* — es el mismo patrón `interface extends XxxRepository` que verás en Spring Data JPA o MongoDB en capítulos futuros con otras bases de datos. Si en el capítulo 2 necesitamos una consulta más compleja (por ejemplo, "productos de una categoría"), se añade como método derivado (`findByCategoriaNombre(String nombre)`) o con `@Query("MATCH ... RETURN ...")` para Cypher explícito — este capítulo no lo necesita todavía.

### 8.4. El adaptador que conecta el puerto de salida con Neo4j

```java
// infraestructura/adaptador/salida/persistencia/adaptador/ProductoRepositorioAdaptador.java
@Component
@RequiredArgsConstructor
public class ProductoRepositorioAdaptador implements ProductoRepositorioPuertoSalida {

	private final ProductoRepositorioNeo4j productoRepositorioNeo4j;
	private final ProductoEntidadMapper productoEntidadMapper;

	@Override
	public Producto guardar(Producto producto) {
		var entidadGuardada = productoRepositorioNeo4j.save(productoEntidadMapper.aEntidad(producto));
		return productoEntidadMapper.aDominio(entidadGuardada);
	}

	@Override
	public Optional<Producto> buscarPorId(ProductoId id) {
		return productoRepositorioNeo4j.findById(id.valor()).map(productoEntidadMapper::aDominio);
	}
}
```

Este es el `implements` que "invierte" la dependencia de la que hablamos en la sección 3: la interfaz (`ProductoRepositorioPuertoSalida`) vive en `aplicacion`, pero quien la implementa vive en `infraestructura` y es el único punto del proyecto que sabe que la persistencia es Neo4j. `ProductoEntidadMapper` hace la traducción en ambas direcciones, y — igual que `ProductoMapper` — usa métodos `default` porque `Producto` no tiene un constructor público ni getters JavaBean; en la dirección entidad→dominio, usa la factory `Producto.reconstruir(...)` en vez de `Producto.crear(...)`, precisamente para no re-disparar la generación de un nuevo id ni de una nueva fecha de creación.

Nota sobre Lombok y este mapper: `producto.id()`, `producto.nombre()`, `producto.precio()` (línea de `aEntidad`) llaman a los getters fluidos que genera `@Getter @Accessors(fluent = true)` en `Producto` (sección 5, punto 3) — antes eran métodos escritos a mano con la misma firma exacta. El mapper no cambió ni una línea al introducir Lombok ahí: como el nombre y la forma del método son idénticos, esta clase (y `ProductoMapper` en la sección 6) son completamente ajenas a si el getter está escrito a mano o generado.

### 8.5. Acceder a los datos manualmente (fuera del código)

A veces quieres mirar el grafo con tus propios ojos, no solo a través de un test. `compose.yaml` publica los dos puertos con mapeo fijo (a diferencia de Testcontainers, aquí nos interesa un puerto estable porque vamos a teclear la URL a mano):

```yaml
services:
  neo4j:
    image: 'neo4j:latest'
    environment:
      - 'NEO4J_AUTH=neo4j/notverysecret'
    ports:
      - '7687:7687'  # Bolt — protocolo binario que usa el driver (Spring Data, cypher-shell)
      - '7474:7474'  # HTTP — sirve la interfaz web de Neo4j Browser
```

**Opción A — `cypher-shell`, la consola interactiva que trae la propia imagen de Neo4j:**

`cypher-shell` es un cliente de línea de comandos que habla Bolt (igual que el driver de Spring Data) y ya viene instalado dentro del contenedor — no hace falta instalar nada en tu máquina.

1. Con el servicio arrancado (`spring-boot:run` levanta `compose.yaml` automáticamente), abre la consola dentro del contenedor:

   ```bash
   docker compose -f servicio-catalogo/compose.yaml exec neo4j cypher-shell -u neo4j -p notverysecret
   ```

2. Si la conexión es correcta, el prompt cambia a `neo4j@neo4j>` — ya estás dentro, escribiendo Cypher directamente.
3. Cada sentencia se ejecuta al pulsar Enter y **debe terminar en `;`**:

   ```
   neo4j@neo4j> MATCH (p:Producto) RETURN p;
   ```

   La salida es una tabla en texto plano con los nodos encontrados (y sus propiedades `id`, `nombre`, `descripcion`, `precio`, `fechaCreacion`).
4. Comandos útiles dentro de la sesión: `:help` (ayuda), `:clear` (limpiar pantalla), `:exit` o `Ctrl+D` (salir y volver a la shell del host).

**Opción B — Neo4j Browser, la interfaz web que trae el propio contenedor:**

1. Con el servicio arrancado (`spring-boot:run` levanta `compose.yaml` automáticamente), abre [http://localhost:7474](http://localhost:7474).
2. En la pantalla de conexión: *Connect URL* → `bolt://localhost:7687`, *Username* → `neo4j`, *Password* → `notverysecret` (las mismas credenciales de `NEO4J_AUTH` en `compose.yaml`).
3. Una vez dentro, la barra superior acepta Cypher directamente. Prueba `MATCH (p:Producto) RETURN p;` y pulsa el botón de ejecutar (▶) — Neo4j Browser dibuja los nodos `:Producto` como un grafo interactivo, mucho más visual que la salida en texto de `cypher-shell`.

![Neo4j Browser mostrando nodos Producto](docs/images/capitulo-01-neo4j-browser.png)

*(Captura pendiente — ver `CHECKLIST.md`, sección "Imágenes pendientes")*

---

## 9. Testing: de los tests unitarios a Testcontainers

### 9.1. Tests unitarios de dominio

`ProductoTest` y `PrecioTest` no arrancan Spring, ni Neo4j, ni nada: son JUnit 5 + AssertJ puro contra clases Java normales, porque el dominio no depende de ningún framework (sección 3). Verifican las invariantes: que un `Precio` negativo lanza excepción, que dos `Producto` con el mismo id son iguales, etc. Corren en milisegundos — son la base de la pirámide de tests.

### 9.2. Testcontainers: qué es y cómo funciona

[Testcontainers](https://testcontainers.com) es una librería que, durante la ejecución de tus tests, **levanta contenedores Docker reales** (una base de datos, un broker, lo que sea) y los destruye al terminar. La alternativa habitual —mockear el repositorio— no te dice nada sobre si tu *query* Cypher, tu mapeo `@Node`/`@Id` o tu configuración de conexión son correctos; un test contra un Neo4j real, sí.

Requisitos y mecánica, según la documentación oficial de Testcontainers:

- **Necesita un daemon Docker accesible** (el mismo Docker que usa `spring-boot-docker-compose` en desarrollo).
- Al arrancar el primer contenedor, Testcontainers también arranca **Ryuk**, el *resource reaper*: un contenedor auxiliar cuyo trabajo es garantizar que ningún contenedor de test quede huérfano — *"Ryuk es responsable de eliminar contenedores y de la limpieza automática de contenedores muertos al apagar la JVM"*. Lo viste en los logs de nuestros tests: `Ryuk started - will monitor and terminate Testcontainers containers on JVM exit`.
- El módulo `org.testcontainers:neo4j` aporta `Neo4jContainer<?>`, que usa la imagen oficial de Neo4j y sabe esperar a que el proceso esté realmente listo antes de devolver el control al test.

Nuestro test de integración:

```java
// test/.../ProductoRepositorioAdaptadorIntegrationTest.java
@DataNeo4jTest
@Testcontainers
@Import({ProductoRepositorioAdaptador.class, ProductoEntidadMapperImpl.class})
class ProductoRepositorioAdaptadorIntegrationTest {

	@Container
	@ServiceConnection
	static final Neo4jContainer<?> neo4jContainer = new Neo4jContainer<>("neo4j:latest");

	@Autowired
	private ProductoRepositorioAdaptador productoRepositorioAdaptador;

	@Test
	void guardaYRecuperaUnProductoPorId() {
		Producto producto = Producto.crear("Camiseta", "100% algodón", Precio.de(new BigDecimal("19.99")));
		productoRepositorioAdaptador.guardar(producto);

		var recuperado = productoRepositorioAdaptador.buscarPorId(producto.id());

		assertThat(recuperado).isPresent();
	}
}
```

Pieza por pieza, apoyándonos en la documentación oficial de Spring Boot:

- **`@Container` como campo `static`**: es la anotación de la extensión JUnit 5 de Testcontainers (activada por `@Testcontainers` en la clase) que le dice "arranca y para este contenedor tú solo" — por eso no hay ningún `@BeforeAll`/`@AfterAll` llamando a `start()`/`stop()` a mano. Al ser un campo `static`, el contenedor se comparte entre todos los métodos de test de la clase: arranca una vez antes del primero y se para una vez después del último. Además, cuando declaras el contenedor como campo estático (en vez de un `@Bean`), *"Spring Boot puede determinar automáticamente el nombre de la imagen Docker... esto funciona para contenedores tipados como `Neo4jContainer`"* — no hace falta decirle a Spring qué tipo de servicio es, lo infiere del tipo Java.
- **`@ServiceConnection`**: *"permite que los detalles de conexión de un servicio en contenedor se generen automáticamente anotando el campo del contenedor en la clase de test"*. Es exactamente el mismo mecanismo que vimos en desarrollo con `spring-boot-docker-compose` (sección 8.1) — por eso no hay ningún `@DynamicPropertySource` manual registrando `spring.neo4j.uri`: Spring Boot lo hace por nosotros en cuanto ve el campo anotado.
- **`@DataNeo4jTest`**: es un *test slice* — *"escanea las clases `@Node` y configura los repositorios de Spring Data Neo4j"*, sin levantar toda la aplicación (controladores, etc.). Además, *"los tests de Data Neo4j son transaccionales y hacen rollback al final de cada test por defecto"*, así que cada método de test empieza con el grafo limpio sin que tengamos que borrar nada manualmente.
- **`@Import({...})`**: `@DataNeo4jTest` no escanea `@Component`/`@Service` por defecto (solo lo estrictamente relacionado con Neo4j), así que importamos explícitamente el adaptador y el mapper generado por MapStruct para poder inyectarlos en el test.

El resultado: un test que se ejecuta en cualquier máquina con Docker, sin un Neo4j instalado a mano, sin mocks, y que se limpia solo — tanto el grafo (rollback por test) como el propio contenedor (Ryuk, al terminar la JVM).

---

## 10. Diagramas

Fuentes editables en `docs/diagramas/` (formato `.excalidraw`, abrir en [excalidraw.com](https://excalidraw.com) o con la extensión de VS Code):

- `docs/diagramas/capitulo-01-arquitectura-hexagonal.excalidraw` — capas dominio/aplicación/infraestructura y dirección de las dependencias.
- `docs/diagramas/capitulo-01-secuencia-crear-producto.excalidraw` — secuencia completa del caso de uso "Crear Producto".

![Arquitectura Hexagonal — servicio-catalogo](docs/images/capitulo-01-arquitectura-hexagonal.png)

![Secuencia — Crear Producto](docs/images/capitulo-01-secuencia-crear-producto.png)

---

## 11. Cómo probarlo de extremo a extremo

```bash
# Tests (unitarios de dominio + integración con Neo4j vía Testcontainers)
./mvnw -pl servicio-catalogo test

# Levantar el servicio (arranca Neo4j vía docker-compose automáticamente)
./mvnw -pl servicio-catalogo spring-boot:run
```

Antes de usarlos, vale la pena entender qué hace cada pieza de estos dos comandos — no son solo "ejecuta tests" / "arranca la app", tienen algunas decisiones del proyecto detrás.

### ¿Qué hace `./mvnw -pl servicio-catalogo test`?

- **`./mvnw`** es el *Maven Wrapper*: un script versionado en el repo (`.mvn/wrapper/`) que descarga y usa la versión exacta de Maven configurada para el proyecto, sin depender de que tengas Maven instalado en el sistema.
- **`-pl servicio-catalogo`** (`--projects`) le dice a Maven qué módulo del reactor construir. Como el `pom.xml` raíz es `packaging=pom` (un *parent* multi-módulo sin código propio, sección 4), hay que apuntar explícitamente al módulo que sí lo tiene. Si `servicio-catalogo` dependiera de otro módulo del mismo reactor, haría falta añadir `-am` (`--also-make`) para construir esas dependencias primero — con un único módulo, no hace falta todavía.
- **`test`** es una *fase* del ciclo de vida estándar de Maven (no un goal suelto): antes de ejecutar los tests, Maven encadena automáticamente las fases anteriores (`compile`, `test-compile`), y en la fase `test` invoca el `maven-surefire-plugin`, que busca y ejecuta todas las clases que cumplen su convención de nombres por defecto (`**/*Test.java`, entre otras). Esto incluye tanto `ProductoTest`/`PrecioTest` (unitarios puros, sin Spring) como `ProductoRepositorioAdaptadorIntegrationTest` (con Testcontainers) — en este proyecto **no** separamos unitarios e integración en fases distintas (`test` vs `verify` con `maven-failsafe-plugin`, que es la convención habitual para tests `*IT.java`); con un único módulo y una sola clase de integración no aporta valor todavía, pero es un candidato a revisar si el número de tests de integración crece en capítulos futuros.

### ¿Qué hace `./mvnw -pl servicio-catalogo spring-boot:run`?

- `./mvnw` y `-pl servicio-catalogo` significan lo mismo que arriba.
- **`spring-boot:run`** ya no es una fase del ciclo de vida, sino un *goal* concreto del `spring-boot-maven-plugin` (configurado en `pluginManagement` del `pom.xml` raíz). Compila el proyecto si hace falta y arranca la aplicación **en el mismo proceso de Maven**, usando el classpath directamente — a diferencia de empaquetar un jar (`package`) y ejecutarlo con `java -jar`, es el camino rápido para desarrollo local. El proceso queda en primer plano mostrando logs hasta que lo paras con `Ctrl+C`.
- El arranque automático de Neo4j **no es cosa de este goal**, sino un efecto colateral de tener `spring-boot-docker-compose` en las dependencias del módulo (sección 8.1): al iniciar la `SpringApplication`, ese módulo busca `servicio-catalogo/compose.yaml`, ejecuta `docker compose up -d` por ti, y crea automáticamente el bean de conexión (`Neo4jConnectionDetails`) apuntando al puerto real que Docker asignó — sin ninguna propiedad `spring.neo4j.uri` manual. Al parar la app con `Ctrl+C`, ejecuta `docker compose stop` (para el contenedor, no lo borra: los datos persisten entre arranques).

Con el servicio arrancado en `http://localhost:8080`:

```bash
# Crear un producto
curl -X POST http://localhost:8080/api/productos \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Camiseta","descripcion":"100% algodón","precio":19.99}'
# -> 201 Created con el ProductoDTO creado (incluye el id generado)

# Buscar un producto por id
curl http://localhost:8080/api/productos/{id}
# -> 200 OK, o 404 si no existe
```

Y, como vimos en la [sección 8.5](#85-acceder-a-los-datos-manualmente-fuera-del-código), puedes confirmar que el dato quedó realmente en el grafo con `cypher-shell` en paralelo.

---

## 12. Qué se deja para el capítulo 2

A propósito, este capítulo **no** cubre:

- `Categoria` como agregado propio y las relaciones de grafo (`Producto` ↔ `Categoria`, recomendaciones producto↔producto) — es el candidato natural del capítulo 2, y es lo que realmente justificará usar Neo4j en vez de una base de datos relacional.
- Consultas Cypher explícitas (`@Query`) — no las necesitamos hasta que haya relaciones que recorrer.
- Resiliencia (`spring-cloud-starter-circuitbreaker-resilience4j` ya está en el `pom.xml`, pero sin usar todavía) — tiene sentido cuando haya más de un microservicio llamándose entre sí.

Consulta `CHECKLIST.md` para el resto del roadmap tecnológico.

---

## 13. Registro de archivos del capítulo

Tabla de control de los archivos que forman el contenido de este capítulo: código del microservicio, diagramas y configuración de build. No incluye archivos internos de desarrollo (`CLAUDE.md`, `CHECKLIST.md`) ni scaffolding de herramientas (skills de Claude Code, Maven wrapper, `.gitignore`/`.gitattributes`), que no aportan valor al lector del tutorial.

**Leyenda:** 🌱 Creado · ✏️ Actualizado · 🗑️ Eliminado

### Documentación y diagramas

| | Archivo | Descripción funcional | Descripción del cambio |
|:---:|---|---|:---:|
| 🌱 | [`docs/diagramas/capitulo-01-arquitectura-hexagonal.excalidraw`](docs/diagramas/capitulo-01-arquitectura-hexagonal.excalidraw) | Fuente editable del diagrama de capas (dominio/aplicación/infraestructura) y dirección de las dependencias. | --- |
| 🌱 | [`docs/diagramas/capitulo-01-secuencia-crear-producto.excalidraw`](docs/diagramas/capitulo-01-secuencia-crear-producto.excalidraw) | Fuente editable del diagrama de secuencia del caso de uso "Crear Producto". | --- |
| 🌱 | [`docs/images/capitulo-01-arquitectura-hexagonal.png`](docs/images/capitulo-01-arquitectura-hexagonal.png) | Render PNG del diagrama de arquitectura hexagonal, embebido en la [sección 10](#10-diagramas). | --- |
| 🌱 | [`docs/images/capitulo-01-secuencia-crear-producto.png`](docs/images/capitulo-01-secuencia-crear-producto.png) | Render PNG del diagrama de secuencia, embebido en la [sección 10](#10-diagramas). | --- |

### Build y configuración

| | Archivo | Descripción funcional | Descripción del cambio |
|:---:|---|---|:---:|
| 🌱 | [`pom.xml`](pom.xml) | Parent Maven multi-módulo: centraliza versiones (Spring Cloud, MapStruct, Testcontainers) y configuración de plugins compartida. | --- |
| 🌱 | [`servicio-catalogo/pom.xml`](servicio-catalogo/pom.xml) | POM del módulo `servicio-catalogo`: declara Spring Web, Spring Data Neo4j, MapStruct y dependencias de test. | --- |
| 🌱 | [`servicio-catalogo/compose.yaml`](servicio-catalogo/compose.yaml) | Levanta un contenedor Neo4j local (puertos Bolt `7687` y HTTP `7474`) vía `spring-boot-docker-compose`. | --- |
| 🌱 | [`servicio-catalogo/src/main/resources/application.properties`](servicio-catalogo/src/main/resources/application.properties) | Configuración de la aplicación, vacía a propósito: la conexión a Neo4j la resuelve `spring-boot-docker-compose`. | --- |
| 🌱 | [`ServicioCatalogoApplication.java`](servicio-catalogo/src/main/java/com/javacadabra/tienda/catalogo/ServicioCatalogoApplication.java) | Clase de arranque de Spring Boot del microservicio. | --- |

### Dominio

| | Archivo | Descripción funcional | Descripción del cambio |
|:---:|---|---|:---:|
| 🌱 | [`Producto.java`](servicio-catalogo/src/main/java/com/javacadabra/tienda/catalogo/dominio/modelo/agregado/Producto.java) | Agregado raíz: encapsula las invariantes de negocio del producto tras las factories `crear`/`reconstruir`. | --- |
| 🌱 | [`Precio.java`](servicio-catalogo/src/main/java/com/javacadabra/tienda/catalogo/dominio/modelo/objetovalor/Precio.java) | Objeto de Valor inmutable que valida que el precio no sea nulo ni negativo. | --- |
| 🌱 | [`ProductoId.java`](servicio-catalogo/src/main/java/com/javacadabra/tienda/catalogo/dominio/modelo/objetovalor/ProductoId.java) | Objeto de Valor que garantiza que el identificador del producto es siempre un UUID válido. | --- |
| 🌱 | [`ProductoNoEncontradoException.java`](servicio-catalogo/src/main/java/com/javacadabra/tienda/catalogo/dominio/excepcion/ProductoNoEncontradoException.java) | Excepción de dominio lanzada cuando no existe un producto con el id solicitado. | --- |

### Aplicación

| | Archivo | Descripción funcional | Descripción del cambio |
|:---:|---|---|:---:|
| 🌱 | [`CrearProductoDTO.java`](servicio-catalogo/src/main/java/com/javacadabra/tienda/catalogo/aplicacion/dto/entrada/CrearProductoDTO.java) | DTO de entrada con los datos necesarios para crear un producto. | --- |
| 🌱 | [`ProductoDTO.java`](servicio-catalogo/src/main/java/com/javacadabra/tienda/catalogo/aplicacion/dto/salida/ProductoDTO.java) | DTO de salida que expone un producto sin filtrar el modelo de dominio. | --- |
| 🌱 | [`ProductoMapper.java`](servicio-catalogo/src/main/java/com/javacadabra/tienda/catalogo/aplicacion/mapper/ProductoMapper.java) | Mapper MapStruct que convierte el agregado `Producto` en `ProductoDTO`. | --- |
| 🌱 | [`BuscarProductoPuertoEntrada.java`](servicio-catalogo/src/main/java/com/javacadabra/tienda/catalogo/aplicacion/puerto/entrada/BuscarProductoPuertoEntrada.java) | Puerto de entrada del caso de uso "buscar producto por id". | --- |
| 🌱 | [`CrearProductoPuertoEntrada.java`](servicio-catalogo/src/main/java/com/javacadabra/tienda/catalogo/aplicacion/puerto/entrada/CrearProductoPuertoEntrada.java) | Puerto de entrada del caso de uso "crear producto". | --- |
| 🌱 | [`ProductoRepositorioPuertoSalida.java`](servicio-catalogo/src/main/java/com/javacadabra/tienda/catalogo/aplicacion/puerto/salida/ProductoRepositorioPuertoSalida.java) | Puerto de salida: lo que la aplicación necesita para persistir y leer productos. | --- |
| 🌱 | [`BuscarProductoServicio.java`](servicio-catalogo/src/main/java/com/javacadabra/tienda/catalogo/aplicacion/servicio/BuscarProductoServicio.java) | Implementa el caso de uso de búsqueda; lanza `ProductoNoEncontradoException` si no existe. | --- |
| 🌱 | [`CrearProductoServicio.java`](servicio-catalogo/src/main/java/com/javacadabra/tienda/catalogo/aplicacion/servicio/CrearProductoServicio.java) | Implementa el caso de uso de creación orquestando dominio, persistencia y mapeo a DTO. | --- |

### Infraestructura de entrada (REST)

| | Archivo | Descripción funcional | Descripción del cambio |
|:---:|---|---|:---:|
| 🌱 | [`ControladorErroresGlobal.java`](servicio-catalogo/src/main/java/com/javacadabra/tienda/catalogo/infraestructura/adaptador/entrada/rest/ControladorErroresGlobal.java) | `@RestControllerAdvice` que traduce excepciones de dominio a códigos HTTP (404, 400). | --- |
| 🌱 | [`ProductoController.java`](servicio-catalogo/src/main/java/com/javacadabra/tienda/catalogo/infraestructura/adaptador/entrada/rest/ProductoController.java) | Adaptador REST: traduce peticiones HTTP a llamadas a los puertos de entrada. | --- |

### Infraestructura de salida (persistencia Neo4j)

| | Archivo | Descripción funcional | Descripción del cambio |
|:---:|---|---|:---:|
| 🌱 | [`ProductoRepositorioAdaptador.java`](servicio-catalogo/src/main/java/com/javacadabra/tienda/catalogo/infraestructura/adaptador/salida/persistencia/adaptador/ProductoRepositorioAdaptador.java) | Adaptador que implementa el puerto de salida usando `ProductoRepositorioNeo4j`. | --- |
| 🌱 | [`ProductoEntidad.java`](servicio-catalogo/src/main/java/com/javacadabra/tienda/catalogo/infraestructura/adaptador/salida/persistencia/entidad/ProductoEntidad.java) | Entidad de persistencia (`@Node`) que representa `Producto` como nodo del grafo Neo4j. | --- |
| 🌱 | [`ProductoEntidadMapper.java`](servicio-catalogo/src/main/java/com/javacadabra/tienda/catalogo/infraestructura/adaptador/salida/persistencia/mapper/ProductoEntidadMapper.java) | Mapper MapStruct entre `ProductoEntidad` y el agregado `Producto` (usa `reconstruir` al leer). | --- |
| 🌱 | [`ProductoRepositorioNeo4j.java`](servicio-catalogo/src/main/java/com/javacadabra/tienda/catalogo/infraestructura/adaptador/salida/persistencia/repositorio/ProductoRepositorioNeo4j.java) | Repositorio Spring Data (`Neo4jRepository`) con las operaciones CRUD básicas. | --- |

### Tests

| | Archivo | Descripción funcional | Descripción del cambio |
|:---:|---|---|:---:|
| 🌱 | [`ProductoTest.java`](servicio-catalogo/src/test/java/com/javacadabra/tienda/catalogo/dominio/modelo/agregado/ProductoTest.java) | Tests unitarios de las invariantes del agregado `Producto`. | --- |
| 🌱 | [`PrecioTest.java`](servicio-catalogo/src/test/java/com/javacadabra/tienda/catalogo/dominio/modelo/objetovalor/PrecioTest.java) | Tests unitarios de las invariantes del Objeto de Valor `Precio`. | --- |
| 🌱 | [`ProductoRepositorioAdaptadorIntegrationTest.java`](servicio-catalogo/src/test/java/com/javacadabra/tienda/catalogo/infraestructura/adaptador/salida/persistencia/ProductoRepositorioAdaptadorIntegrationTest.java) | Test de integración con un Neo4j real vía Testcontainers. | --- |

---

## 14. Referencias

- [Spring Boot Reference — NoSQL Data Access (Neo4j)](https://docs.spring.io/spring-boot/4.1.0/reference/data/nosql.html#data.nosql.neo4j)
- [Spring Boot Reference — Docker Compose Support](https://docs.spring.io/spring-boot/4.1.0/reference/features/dev-services.html#features.dev-services.docker-compose)
- [Spring Boot Reference — Testcontainers en tests](https://docs.spring.io/spring-boot/4.1.0/reference/testing/testcontainers.html)
- [Testcontainers for Java — módulo Neo4j](https://java.testcontainers.org/modules/databases/neo4j/)
- [Testcontainers for Java — Ryuk / limpieza de recursos](https://java.testcontainers.org/features/configuration/)

Ver también [CLAUDE.md](CLAUDE.md) para la convención arquitectónica completa y el modelo de ramas del proyecto, y [CHECKLIST.md](CHECKLIST.md) para el estado de tecnologías cubiertas.
