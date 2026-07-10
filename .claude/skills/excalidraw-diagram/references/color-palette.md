# Paleta de colores — diagramas de este proyecto

Reconstruida a partir de los diagramas ya publicados (`docs/diagramas/capitulo-01-*.excalidraw`, `capitulo-02-*.excalidraw`), no del `color-palette.md` original del skill (perdido — ver nota en `SKILL.md`). Úsala como fuente única de verdad para cualquier diagrama nuevo de este tutorial, para que todos compartan el mismo lenguaje visual.

## Regla general

- `roughness: 0` (trazo limpio, no manuscrito) en todas las formas.
- `opacity: 100` siempre — la jerarquía se marca con color/tamaño, no con transparencia.
- `strokeWidth: 2` para formas y flechas principales; `1` para contenedores de evidencia (cajas de código).
- `roundness: { "type": 2 }` en rectángulos normales; `{ "type": 3 }` en cajas de evidencia/código, como diferenciador sutil.
- `fontFamily`: **solo 1, 2 o 3** — nunca 4 o superior (el id 5 deja el texto invisible en el plugin de Excalidraw de IntelliJ, que es como se revisan estos diagramas en este proyecto).
  - `1` (Virgil, manuscrita) — título, subtítulo, etiquetas de relación, texto descriptivo en general.
  - `3` (Code) — cualquier cosa que sea literalmente código o esquema tabular (columnas de una tabla, fragmentos SQL/JSON).
  - `2` (Normal) no se ha necesitado todavía en este proyecto; disponible si hace falta un texto limpio no-manuscrito que no sea código.

## Semántica de color por concepto

| Concepto | Stroke | Fill (relleno fuerte, p. ej. cabecera) | Fill (relleno pálido, p. ej. cuerpo) | Texto |
|---|---|---|---|---|
| Entidad "lado uno" / raíz / padre (p. ej. `Categoria`, `pedidos`) | `#e67700` (naranja) | `#ffd43b` (amarillo) | `#fff3bf` (amarillo pálido) | `#5c3c00` |
| Entidad "lado muchos" / hijo (p. ej. `Producto`, `lineas_pedido`) | `#1864ab` (azul) | `#a5d8ff` (azul claro) | `#d0ebff` (azul muy pálido) | `#0b3866` |
| Relación estructural (FK, `PERTENECE_A`) | `#1e1e1e` (negro) | — | — | `#1e1e1e` sobre fondo `#ffffff` |
| Relación alternativa/secundaria (p. ej. `RELACIONADO_CON`, discrecional) | `#7950f2` (violeta), `strokeStyle: "dashed"` | — | — | `#5f3dc4` sobre fondo `#ffffff` |
| Título | — | — | — | `#1e1e1e`, `fontSize: 28` |
| Subtítulo / texto secundario / caption | — | — | — | `#495057`, `fontSize: 14-16` |
| Evidencia de código (SQL, JSON, curl) | `#343a40` (borde caja) | `#212529` (fondo caja) | — | `#8ce99a` (verde suave), `fontFamily: 3` |

**Por qué "uno=amarillo/naranja, muchos=azul"**: convención fijada en `capitulo-02-modelo-grafo-categoria.excalidraw` (Categoría=amarillo, Producto=azul) y reutilizada en `capitulo-06-modelo-relacional-pedidos.excalidraw` (pedidos=amarillo, lineas_pedido=azul) — mantenerla en diagramas futuros para que el color por sí solo ya comunique qué lado de una relación 1:N es cada bloque, sin tener que leer las etiquetas.

## Semántica de color — estado de salud / máquinas de estados

Categoría distinta de la anterior (esa es para relaciones "uno/muchos" en diagramas tipo ER; esta es para nodos que representan un estado de salud/disponibilidad, como en un circuit breaker). No reutilizar el amarillo/naranja de "lado uno" aquí — significaría algo distinto y confundiría a quien ya conoce esa convención.

| Estado | Stroke | Fill | Texto |
|---|---|---|---|
| Saludable / cerrado / OK (p. ej. `CLOSED`) | `#2b8a3e` (verde) | `#d3f9d8` (verde pálido) | `#1b4332` |
| Fallando / abierto / bloqueado (p. ej. `OPEN`) | `#c92a2a` (rojo) | `#ffc9c9` (rojo pálido) | `#7d1a1a` |
| Transición / probando / advertencia (p. ej. `HALF_OPEN`) | `#f08c00` (ámbar) | `#ffe8cc` (ámbar pálido) | `#7c4a03` |

Fijada en `capitulo-08-circuit-breaker-estados.excalidraw` (máquina de estados `CLOSED`/`OPEN`/`HALF_OPEN` de Resilience4j) — semáforo universal (verde/ámbar/rojo), reutilizable en cualquier diagrama futuro que necesite representar un estado de salud o disponibilidad (p. ej. dashboards de observabilidad en capítulos posteriores).

## Notas de renderizado

- Un elemento de texto con `backgroundColor` **no siempre se pinta de forma fiable** por debajo de una flecha que pasa justo por encima (comprobado en `capitulo-06`) — mejor separar la etiqueta de la línea (por encima o por debajo, sin cruzarla) que confiar en que el fondo blanco la tape.
- Deja hueco suficiente entre bloques principales si va a haber una etiqueta de relación en medio (mínimo ~250px de separación horizontal para una etiqueta de un par de palabras a `fontSize: 13-14`).
