# /// script
# requires-python = ">=3.10"
# dependencies = [
#   "playwright>=1.45",
#   "pillow>=10.0",
# ]
# ///
"""Renderiza un fichero .excalidraw a PNG abriéndolo en excalidraw.com con un
navegador headless (Playwright) y recortando automáticamente los márgenes en
blanco alrededor del contenido.

No usa el editor de escritorio ni ningún servidor propio: excalidraw.com no
expone una API de importación por input[type=file] en navegadores headless
modernos (usa la File System Access API, `showOpenFilePicker`), así que ese
método se sustituye por un mock que "abre" directamente el fichero local.

Primer uso en una máquina nueva:
    cd .claude/skills/excalidraw-diagram/references
    uv run playwright install chromium

Uso:
    uv run render_excalidraw.py <entrada.excalidraw> [salida.png]

Si no se indica la salida, se usa el mismo nombre con extensión .png junto
al fichero de entrada.
"""

import json
import sys
from pathlib import Path

from playwright.sync_api import sync_playwright
from PIL import Image, ImageChops

MOCK_FILE_PICKER = """
window.showOpenFilePicker = async () => {
	const file = new File([%s], %s, { type: "application/json" });
	return [{
		kind: "file",
		name: %s,
		getFile: async () => file,
		queryPermission: async () => "granted",
		requestPermission: async () => "granted",
	}];
};
"""


def render(input_path: Path, output_path: Path) -> None:
	content = input_path.read_text(encoding="utf-8")
	# JSON.stringify de cada valor para inyectarlo de forma segura en el script inyectado
	init_script = MOCK_FILE_PICKER % (
		json.dumps(content),
		json.dumps(input_path.name),
		json.dumps(input_path.name),
	)

	with sync_playwright() as p:
		browser = p.chromium.launch()
		page = browser.new_page(viewport={"width": 1920, "height": 1200})
		page.add_init_script(init_script)

		page.goto("https://excalidraw.com", wait_until="networkidle")
		page.wait_for_timeout(1200)

		# El mock de showOpenFilePicker resuelve la promesa sin diálogo nativo
		# ni evento 'filechooser' — un click normal basta.
		page.click('button:has-text("Open")')
		page.wait_for_timeout(1200)

		# Deselecciona y centra el zoom sobre todo el contenido (Shift+1)
		page.mouse.click(20, 20)
		page.keyboard.press("Escape")
		page.wait_for_timeout(150)
		page.keyboard.press("Shift+1")
		page.wait_for_timeout(400)
		page.keyboard.press("Escape")
		page.wait_for_timeout(150)

		# Oculta toda la UI de Excalidraw (toolbar, zoom, ayuda) para que la
		# captura contenga solo el lienzo dibujado
		page.add_style_tag(content=".layer-ui__wrapper { display: none !important; }")
		page.wait_for_timeout(150)

		raw_path = output_path.with_suffix(".raw.png")
		page.screenshot(path=str(raw_path))
		browser.close()

	_autocrop(raw_path, output_path)
	raw_path.unlink()
	print(f"Renderizado: {output_path}")


def _autocrop(raw_path: Path, output_path: Path, margin: int = 24) -> None:
	image = Image.open(raw_path).convert("RGB")
	background = Image.new("RGB", image.size, (255, 255, 255))
	diff = ImageChops.difference(image, background)
	bbox = diff.getbbox()
	if bbox is None:
		image.save(output_path)
		return
	left, top, right, bottom = bbox
	left = max(0, left - margin)
	top = max(0, top - margin)
	right = min(image.width, right + margin)
	bottom = min(image.height, bottom + margin)
	image.crop((left, top, right, bottom)).save(output_path)


if __name__ == "__main__":
	if len(sys.argv) < 2:
		print("Uso: uv run render_excalidraw.py <entrada.excalidraw> [salida.png]", file=sys.stderr)
		sys.exit(1)

	input_path = Path(sys.argv[1]).resolve()
	output_path = Path(sys.argv[2]).resolve() if len(sys.argv) > 2 else input_path.with_suffix(".png")
	render(input_path, output_path)
