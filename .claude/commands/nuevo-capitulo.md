---
description: Arranca un capítulo nuevo del tutorial — crea la rama desde la base correcta, pregunta qué tecnología se va a mostrar y monta el esqueleto mínimo del README.
---

# Nuevo capítulo del tutorial

Vas a arrancar un capítulo nuevo de "De cero a pro en arquitectura de microservicios con Spring Boot". Este comando automatiza solo la parte **mecánica y repetitiva** del arranque — la rama y el esqueleto del README —, no el contenido técnico del capítulo: eso se sigue escribiendo en conversación, como en todos los capítulos anteriores.

Sigue estos pasos en orden.

## 1. Verifica el estado del repo

- `git status` — si hay cambios sin commitear en la rama actual, párate y avisa al usuario antes de cambiar de rama (nunca descartes ni mezcles trabajo a medias).
- `git branch --show-current` — confirma en qué rama estás ahora mismo.

## 2. Pregunta al usuario (usa el tool `AskUserQuestion`, no asumas nada)

1. **Número y slug del capítulo**: qué número de capítulo es y cómo se llamará la rama (`capitulo-NN-slug-en-espanol`, en español, kebab-case).
2. **Tecnología o concepto a mostrar**: qué tema nuevo introduce este capítulo (p. ej. "eventos de dominio", "OpenAPI/Swagger", "Kafka", "ArchUnit").
3. **Tipo de capítulo**:
   - **Acumulativo** (lo habitual): parte de la rama del capítulo anterior, hereda `CHECKLIST.md` y todo el código previo.
   - **Solo documentación** (como el capítulo 0): parte de `main` de forma independiente, sin encadenarse, y **sin `CHECKLIST.md` propio** — ver la excepción documentada en `CLAUDE.md`, sección "Modelo de ramas (acumulativo)".
4. Si es acumulativo: **¿desde qué rama exactamente parte?** — sugiere la rama actual como valor por defecto, pero confírmalo explícitamente (podría no ser la más reciente si hay capítulos ya creados pero aún sin revisar).

## 3. Crea la rama

- Acumulativo: `git checkout <rama-base>` y luego `git checkout -b capitulo-NN-slug-en-espanol`.
- Solo documentación: `git checkout main` y luego `git checkout -b capitulo-NN-slug-en-espanol`.

No hagas `push` en este paso — el push, como el resto del proyecto, solo se hace cuando el usuario lo pide explícitamente.

## 4. Monta el esqueleto del `README.md`

Crea un `README.md` nuevo (no lo generes completo — es un punto de partida, el contenido real se escribe después, capítulo a capítulo) con:

- Título: `# Capítulo NN — <título derivado de la tecnología a mostrar>`.
- Una línea de contexto enlazando al índice de capítulos en `main`.
- Una sección `## Índice` vacía (se rellena al final, cuando ya se conocen todas las secciones reales del capítulo).
- Un separador `---`.
- Un comentario o placeholder indicando dónde va el contenido (no inventes secciones de contenido: dependen del tema que se decida en la pregunta 2, y se desarrollan conversacionalmente).
- Como **penúltima sección**: `## N. Registro de archivos del capítulo`, con la leyenda de iconos (🌱 Creado · ✏️ Actualizado · 🗑️ Eliminado) y sin filas todavía — ver convención en `CLAUDE.md`, sección "Convención: registro de archivos por capítulo".
- Como **última sección**: `## N+1. Referencias`, vacía.

## 5. Si es un capítulo acumulativo normal, recuerda (no lo hagas tú solo, avisa al usuario)

- `CHECKLIST.md` ya viene heredado de la rama base — se actualiza a medida que el capítulo cubre temas nuevos, como parte del mismo commit que los introduce.
- `GLOSARIO.md`: si el capítulo introduce un término técnico nuevo, se añade esa fila al glosario en el mismo commit que lo introduce por primera vez en el README (ver `CLAUDE.md`, sección "Idioma y lenguaje ubicuo").

## 6. No commitees automáticamente

Deja el estado preparado (rama creada + esqueleto de README) y pregunta al usuario antes de hacer el primer commit — igual que en el resto del proyecto, nunca se commitea sin petición explícita.

## Convenciones de formato ya establecidas (aplícalas desde la primera línea que escribas)

- Notas explicativas en blockquote con encabezado en negrita y párrafos separados donde tenga sentido — ver `CLAUDE.md`, sección "Convención: notas explicativas en el README".
- Cualquier referencia a otra sección o subsección del propio README va como enlace markdown (`[texto](#ancla)`), nunca como texto plano ("sección 6").
- Toda imagen embebida lleva debajo, en su propio párrafo (línea en blanco de por medio), un pie de foto en cursiva describiendo qué se ve, y un `<br>` extra antes de continuar con el siguiente párrafo.
- La primera vez que aparece un término técnico nuevo en el README, se escribe la traducción al español seguida del término en inglés entre paréntesis; las siguientes apariciones usan solo la forma en español ya fijada (ver `GLOSARIO.md`).