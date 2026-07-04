# Capítulo 1 — Fundamentos DDD + Arquitectura Hexagonal

Primer capítulo del libro de aprendizaje "De cero a pro en arquitectura de microservicios con Spring Boot" (ver el índice completo de capítulos en la rama `main`).

## Qué se construye

El primer microservicio de la tienda online: **`servicio-catalogo`** (gestión de productos). Sirve como referencia arquitectónica DDD/Hexagonal para todos los microservicios que se añadan en capítulos futuros.

El repositorio pasa a ser un **monorepo multi-módulo Maven**: el `pom.xml` raíz es ahora el parent (`packaging=pom`) y `servicio-catalogo` es el primer módulo hijo.

## Decisiones de diseño

- **Arquitectura Hexagonal**: el dominio (`dominio`) no depende de ningún framework. La capa de aplicación (`aplicacion`) expone casos de uso como puertos de entrada (`...PuertoEntrada`) y depende de abstracciones para la persistencia (`...PuertoSalida`). La infraestructura (`infraestructura`) implementa esos puertos con adaptadores concretos (REST de entrada, Neo4j de salida).
- **Lenguaje ubicuo en español**: el modelo de dominio (`Producto`, `Precio`, `ProductoId`, `ProductoNoEncontradoExcepcion`) usa nombres en español. Las capas técnicas mantienen sufijos en inglés donde es el estándar del framework (`Controller`, `Service`, `Repository`).
- **Alcance reducido a propósito**: este capítulo solo cubre el agregado `Producto` con creación y búsqueda por id. Se dejó fuera `Categoria` y las relaciones de grafo (recomendaciones, pertenencia a categoría) para no sobre-diseñar el primer capítulo — es el candidato natural del capítulo 2, y es lo que realmente aprovechará Neo4j como base de datos de grafos.
- **Value Objects como `record`**: `ProductoId` y `Precio` son inmutables y se auto-validan en su constructor canónico.
- **MapStruct** para los mappers dominio↔DTO y dominio↔entidad de persistencia, con métodos `default` cuando el dominio no expone getters estilo JavaBean (por diseño, para mantener el agregado encapsulado).

## Estructura del módulo `servicio-catalogo`

```
dominio/
  modelo/agregado/Producto.java
  modelo/objetovalor/ProductoId.java, Precio.java
  excepcion/ProductoNoEncontradoExcepcion.java
aplicacion/
  puerto/entrada/CrearProductoPuertoEntrada.java, BuscarProductoPuertoEntrada.java
  puerto/salida/ProductoRepositorioPuertoSalida.java
  servicio/CrearProductoServicio.java, BuscarProductoServicio.java
  dto/entrada/CrearProductoDTO.java, dto/salida/ProductoDTO.java
  mapper/ProductoMapper.java
infraestructura/
  adaptador/entrada/rest/ProductoController.java, ControladorErroresGlobal.java
  adaptador/salida/persistencia/entidad/ProductoEntidad.java
  adaptador/salida/persistencia/repositorio/ProductoRepositorioNeo4j.java
  adaptador/salida/persistencia/mapper/ProductoEntidadMapper.java
  adaptador/salida/persistencia/adaptador/ProductoRepositorioAdaptador.java
```

## Diagramas

Fuentes editables en `docs/diagramas/` (formato `.excalidraw`, abrir en [excalidraw.com](https://excalidraw.com) o con la extensión de VS Code):

- `docs/diagramas/capitulo-01-arquitectura-hexagonal.excalidraw` — capas dominio/aplicación/infraestructura y dirección de las dependencias.
- `docs/diagramas/capitulo-01-secuencia-crear-producto.excalidraw` — secuencia completa del caso de uso "Crear Producto".

![Arquitectura Hexagonal — servicio-catalogo](docs/images/capitulo-01-arquitectura-hexagonal.png)

![Secuencia — Crear Producto](docs/images/capitulo-01-secuencia-crear-producto.png)

## Cómo probarlo

```bash
# Tests (unitarios de dominio + integración con Neo4j vía Testcontainers)
./mvnw -pl servicio-catalogo test

# Levantar el servicio (arranca Neo4j vía docker-compose automáticamente)
./mvnw -pl servicio-catalogo spring-boot:run
```

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

Ver [CLAUDE.md](CLAUDE.md) para la convención arquitectónica completa y el modelo de ramas del proyecto, y [CHECKLIST.md](CHECKLIST.md) para el estado de tecnologías cubiertas.
