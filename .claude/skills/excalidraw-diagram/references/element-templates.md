# Plantillas de elementos — Excalidraw JSON

Reconstruidas a partir de los diagramas ya publicados de este proyecto (no el `element-templates.md` original del skill — ver nota en `SKILL.md`). Copia el bloque que necesites y ajusta `id`, coordenadas, `text` e `index`/`seed`/`versionNonce` (deben ser únicos dentro del fichero; basta con incrementar un contador).

Colores: ver `color-palette.md`. Todas las plantillas usan `roughness: 0`, `opacity: 100`.

## Campos obligatorios para que el texto se vea en el plugin de IntelliJ

**Ningún elemento debe llevar `"boundElements": null`** — usa siempre `"boundElements": []` (array vacío), tanto en formas como en texto. Y **todo elemento `text` debe incluir `baseline`** además de `originalText`/`autoResize: true`/`lineHeight` (ya exigidos antes). Sin esto, el diagrama se ve bien en VS Code y en el render headless de esta skill, pero el texto queda invisible en el plugin de Excalidraw de IntelliJ — confirmado en el capítulo 9 del tutorial (ver memoria `feedback-excalidraw-baseline-field`). Las plantillas de abajo ya lo incluyen; no las quites al copiar.

Fórmula para calcular `baseline` de un elemento `text`:

```
baseline = round((n_lineas - 1) * fontSize * lineHeight + fontSize * 0.85, 1)
```

donde `n_lineas` es el número de `\n` en `text` más 1. No hace falta exactitud de píxel — es una aproximación suficiente para que el campo esté presente con un valor plausible.

## Texto libre (sin contenedor)

Para títulos, subtítulos, captions y etiquetas de relación.

```json
{
  "id": "mi_texto",
  "type": "text",
  "x": 150, "y": 20, "width": 400, "height": 30,
  "angle": 0,
  "strokeColor": "#1e1e1e",
  "backgroundColor": "transparent",
  "fillStyle": "solid",
  "strokeWidth": 2,
  "strokeStyle": "solid",
  "roughness": 0,
  "opacity": 100,
  "groupIds": [], "frameId": null, "index": "a0", "roundness": null,
  "seed": 100001, "version": 1, "versionNonce": 200001,
  "isDeleted": false, "boundElements": [], "updated": 1751100000000,
  "link": null, "locked": false,
  "text": "Texto del elemento",
  "originalText": "Texto del elemento",
  "fontSize": 16,
  "fontFamily": 1,
  "textAlign": "center",
  "verticalAlign": "middle",
  "containerId": null,
  "lineHeight": 1.25,
  "autoResize": true,
  "baseline": 14
}
```

`baseline` de este ejemplo: 1 línea, `fontSize: 16` → `round(16 * 0.85, 1)` = `13.6` (redondeado aquí a `14` por legibilidad; cualquiera de los dos vale).

## Rectángulo con texto centrado (cabecera de entidad)

Dos elementos: el rectángulo y el texto con `containerId` apuntando a él. El rectángulo debe listar el texto en su `boundElements`.

> **Prefiere texto libre cuando puedas.** Este patrón (texto ligado a contenedor) funciona, pero añade una variable más al render en IntelliJ — si el diagrama no necesita que el texto se mueva junto con la forma al arrastrarla en el editor, usa el patrón de "texto libre" de arriba posicionado manualmente encima, es más robusto.

```json
{
  "id": "mi_caja",
  "type": "rectangle",
  "x": 150, "y": 160, "width": 280, "height": 50,
  "angle": 0,
  "strokeColor": "#e67700",
  "backgroundColor": "#ffd43b",
  "fillStyle": "solid",
  "strokeWidth": 2,
  "strokeStyle": "solid",
  "roughness": 0,
  "opacity": 100,
  "groupIds": [], "frameId": null, "index": "a1", "roundness": { "type": 2 },
  "seed": 100002, "version": 1, "versionNonce": 200002,
  "isDeleted": false,
  "boundElements": [{ "id": "mi_caja_texto", "type": "text" }],
  "updated": 1751100000000, "link": null, "locked": false
},
{
  "id": "mi_caja_texto",
  "type": "text",
  "x": 160, "y": 172.5, "width": 260, "height": 25,
  "angle": 0,
  "strokeColor": "#5c3c00",
  "backgroundColor": "transparent",
  "fillStyle": "solid",
  "strokeWidth": 2,
  "strokeStyle": "solid",
  "roughness": 0,
  "opacity": 100,
  "groupIds": [], "frameId": null, "index": "a1z", "roundness": null,
  "seed": 100003, "version": 1, "versionNonce": 200003,
  "isDeleted": false, "boundElements": [], "updated": 1751100000000,
  "link": null, "locked": false,
  "text": "nombre",
  "originalText": "nombre",
  "fontSize": 20,
  "fontFamily": 1,
  "textAlign": "center",
  "verticalAlign": "middle",
  "containerId": "mi_caja",
  "lineHeight": 1.25,
  "autoResize": true,
  "baseline": 17
}
```

Cálculo rápido para centrar el texto en la caja: `text.x = caja.x + 10`, `text.width = caja.width - 20`, `text.height = fontSize * 1.25` (una línea), `text.y = caja.y + (caja.height - text.height) / 2`.

## Rectángulo con lista de campos (cuerpo de tabla/entidad)

El rectángulo del cuerpo va **sin** `boundElements` (array vacío) — el texto va libre encima, alineado a la izquierda, no centrado, para que lea como una lista de columnas.

```json
{
  "id": "mi_cuerpo",
  "type": "rectangle",
  "x": 150, "y": 210, "width": 320, "height": 140,
  "strokeColor": "#e67700", "backgroundColor": "#fff3bf",
  "fillStyle": "solid", "strokeWidth": 2, "strokeStyle": "solid",
  "roughness": 0, "opacity": 100,
  "groupIds": [], "frameId": null, "index": "a2", "roundness": { "type": 2 },
  "seed": 100004, "version": 1, "versionNonce": 200004,
  "isDeleted": false, "boundElements": [], "updated": 1751100000000,
  "link": null, "locked": false, "angle": 0
},
{
  "id": "mi_cuerpo_campos",
  "type": "text",
  "x": 170, "y": 246, "width": 290, "height": 68,
  "strokeColor": "#5c3c00", "backgroundColor": "transparent",
  "fillStyle": "solid", "strokeWidth": 2, "strokeStyle": "solid",
  "roughness": 0, "opacity": 100,
  "groupIds": [], "frameId": null, "index": "a2z", "roundness": null,
  "seed": 100005, "version": 1, "versionNonce": 200005,
  "isDeleted": false, "boundElements": [], "updated": 1751100000000,
  "link": null, "locked": false, "angle": 0,
  "text": "PK  id               VARCHAR(36)\n    otro_campo       TIPO",
  "originalText": "PK  id               VARCHAR(36)\n    otro_campo       TIPO",
  "fontSize": 15,
  "fontFamily": 3,
  "textAlign": "left",
  "verticalAlign": "top",
  "containerId": null,
  "lineHeight": 1.5,
  "autoResize": true,
  "baseline": 32
}
```

Anchura del texto: deja al menos ~10px de padding a cada lado dentro del rectángulo. Con `fontFamily: 3` (Code) a `fontSize: 15`, cada carácter ocupa ≈9-9.5px — para una línea de N caracteres, ancho mínimo ≈ `N * 9.5 + 20`.

## Flecha con etiqueta

La flecha y su etiqueta son elementos separados; la etiqueta **no** debe superponerse a la línea (ver nota en `color-palette.md` sobre `backgroundColor` en texto).

```json
{
  "id": "mi_flecha",
  "type": "arrow",
  "x": 650, "y": 305, "width": 180, "height": 25,
  "angle": 0,
  "strokeColor": "#1e1e1e", "backgroundColor": "transparent",
  "fillStyle": "solid", "strokeWidth": 2, "strokeStyle": "solid",
  "roughness": 0, "opacity": 100,
  "groupIds": [], "frameId": null, "index": "a3", "roundness": { "type": 2 },
  "seed": 100006, "version": 1, "versionNonce": 200006,
  "isDeleted": false, "boundElements": [], "updated": 1751100000000,
  "link": null, "locked": false,
  "points": [[0, 0], [-180, -25]],
  "startBinding": null,
  "endBinding": null,
  "startArrowhead": null,
  "endArrowhead": "arrow"
}
```

`points` son coordenadas **relativas** al `x`/`y` de la flecha (el primer punto casi siempre `[0, 0]`). El origen (`x`,`y`) es la cola; el punto final define dónde apunta la cabeza.

## Caja de evidencia (código/SQL/JSON)

```json
{
  "id": "mi_codigo_bg",
  "type": "rectangle",
  "x": 330, "y": 450, "width": 520, "height": 145,
  "angle": 0,
  "strokeColor": "#343a40", "backgroundColor": "#212529",
  "fillStyle": "solid", "strokeWidth": 1, "strokeStyle": "solid",
  "roughness": 0, "opacity": 100,
  "groupIds": [], "frameId": null, "index": "a4", "roundness": { "type": 3 },
  "seed": 100007, "version": 1, "versionNonce": 200007,
  "isDeleted": false, "boundElements": [], "updated": 1751100000000,
  "link": null, "locked": false
},
{
  "id": "mi_codigo_texto",
  "type": "text",
  "x": 350, "y": 462, "width": 480, "height": 120,
  "angle": 0,
  "strokeColor": "#8ce99a", "backgroundColor": "transparent",
  "fillStyle": "solid", "strokeWidth": 2, "strokeStyle": "solid",
  "roughness": 0, "opacity": 100,
  "groupIds": [], "frameId": null, "index": "a4z", "roundness": null,
  "isDeleted": false, "boundElements": [], "updated": 1751100000000,
  "link": null, "locked": false,
  "text": "CREATE TABLE ejemplo (\n  id UUID PRIMARY KEY\n);",
  "originalText": "CREATE TABLE ejemplo (\n  id UUID PRIMARY KEY\n);",
  "fontSize": 14,
  "fontFamily": 3,
  "textAlign": "left",
  "verticalAlign": "top",
  "containerId": null,
  "lineHeight": 1.4,
  "autoResize": true,
  "baseline": 30,
  "seed": 100008, "version": 1, "versionNonce": 200008
}
```

## Checklist antes de guardar

- IDs únicos en todo el fichero (`id`, y en texto libre no hace falta `containerId`).
- `seed`/`versionNonce` únicos por elemento (basta un contador incremental).
- `index` en orden ascendente aproximado al orden de creación (define el z-order; usa sufijo `z` para el texto ligado justo después de su contenedor, p. ej. `a1` → `a1z`).
- Ningún `fontFamily` fuera de `{1, 2, 3}`.
- **Ningún `"boundElements": null`** — siempre `[]`.
- **Todo elemento `text` con `baseline`** calculado (fórmula arriba), además de `originalText`/`autoResize: true`/`lineHeight`.
- Render y validación con `render_excalidraw.py` antes de dar el diagrama por terminado (ver `SKILL.md`).