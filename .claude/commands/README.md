# Comandos personalizados de Claude Code

Catálogo de los comandos (`.claude/commands/*.md`, invocables como `/nombre-del-comando`) creados para el flujo de trabajo de este repositorio. No es contenido del tutorial — es documentación interna de desarrollo, igual que `CLAUDE.md` o `CHECKLIST.md` (por eso vive dentro de `.claude/`, no en la raíz del proyecto, y por eso no aparece en la sección "Registro de archivos del capítulo" de ningún README de capítulo).

Cada entrada explica **qué hace el comando, cuándo usarlo y qué NO hace** — para que quede claro dónde termina la automatización y dónde empieza el trabajo conversacional de siempre.

---

## `/nuevo-capitulo`

**Archivo**: [`nuevo-capitulo.md`](nuevo-capitulo.md)

**Qué hace**: automatiza el arranque *mecánico* de un capítulo nuevo del tutorial:

1. Comprueba que no haya cambios sin commitear antes de cambiar de rama.
2. Pregunta (con `AskUserQuestion`, nunca asume) el número/slug del capítulo, qué tecnología o concepto se va a mostrar, si es un capítulo acumulativo o de solo documentación (como el capítulo 0), y desde qué rama debe partir.
3. Crea la rama con el nombre correcto desde la base correcta.
4. Monta un `README.md` esqueleto: título, hueco de índice, y las dos secciones de cierre fijas (Registro de archivos del capítulo, Referencias) — sin inventar las secciones de contenido intermedias.
5. Recuerda (sin hacerlo por su cuenta) actualizar `CHECKLIST.md` y revisar `GLOSARIO.md` a medida que avance el capítulo.

**Cuándo usarlo**: justo después de dar por cerrado y revisado el capítulo anterior, al empezar uno nuevo. Sustituye el ritual manual de:

```bash
git checkout <rama-del-capitulo-anterior>
git checkout -b capitulo-NN-slug-en-espanol
```

seguido de copiar a mano la estructura de un README de capítulo previo y adaptarla.

**Qué NO hace** (a propósito):

- **No escribe el contenido técnico del capítulo.** Las secciones intermedias del README (dominio, aplicación, infraestructura, testing, etc.) dependen por completo de la tecnología que se decida mostrar, y se desarrollan igual que hasta ahora: a través de la conversación, sección a sección.
- **No hace commit ni push.** Deja la rama creada y el esqueleto del README preparados, y espera confirmación explícita antes de commitear — igual que en cualquier otra tarea de este proyecto.
- **No actualiza `CHECKLIST.md` ni `GLOSARIO.md` por su cuenta** — solo recuerda que hay que hacerlo, porque ambos dependen de contenido que todavía no existe en el momento de arrancar el capítulo.

**Disponibilidad**: presente en `main`, `capitulo-01-fundamentos-ddd-hexagonal` y `capitulo-02-relaciones-de-grafo-categoria`. Al ser un archivo de Git normal, cualquier rama nueva que parta de una de estas lo hereda automáticamente; si en el futuro se crea una rama de capítulo a partir de otra que no lo tenga, hay que traerlo a mano (`git checkout <rama-con-el-comando> -- .claude/commands/`) o mediante un merge.

---

*(Cuando se añadan comandos nuevos, cada uno se documenta aquí con esta misma estructura: qué hace, cuándo usarlo, qué no hace, y en qué ramas está disponible.)*