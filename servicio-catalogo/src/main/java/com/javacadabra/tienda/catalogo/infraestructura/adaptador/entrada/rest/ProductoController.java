package com.javacadabra.tienda.catalogo.infraestructura.adaptador.entrada.rest;

import com.javacadabra.tienda.catalogo.aplicacion.dto.entrada.CrearProductoDTO;
import com.javacadabra.tienda.catalogo.aplicacion.dto.entrada.RecomendarProductoDTO;
import com.javacadabra.tienda.catalogo.aplicacion.dto.salida.ProductoDTO;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada.BuscarProductoPuertoEntrada;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada.BuscarProductosPorCategoriaPuertoEntrada;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada.BuscarProductosRecomendadosPuertoEntrada;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada.CrearProductoPuertoEntrada;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada.RecomendarProductoPuertoEntrada;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController implements ProductoApiDoc {

	private final CrearProductoPuertoEntrada crearProductoPuertoEntrada;
	private final BuscarProductoPuertoEntrada buscarProductoPuertoEntrada;
	private final BuscarProductosPorCategoriaPuertoEntrada buscarProductosPorCategoriaPuertoEntrada;
	private final RecomendarProductoPuertoEntrada recomendarProductoPuertoEntrada;
	private final BuscarProductosRecomendadosPuertoEntrada buscarProductosRecomendadosPuertoEntrada;

	@Override
	@PostMapping
	public ResponseEntity<ProductoDTO> crear(@RequestBody CrearProductoDTO dto) {
		ProductoDTO creado = crearProductoPuertoEntrada.crear(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(creado);
	}

	@Override
	@GetMapping("/{id}")
	public ResponseEntity<ProductoDTO> buscarPorId(@PathVariable String id) {
		return ResponseEntity.ok(buscarProductoPuertoEntrada.buscarPorId(id));
	}

	@Override
	@GetMapping(params = "categoriaId")
	public ResponseEntity<List<ProductoDTO>> buscarPorCategoria(@RequestParam String categoriaId) {
		return ResponseEntity.ok(buscarProductosPorCategoriaPuertoEntrada.buscarPorCategoria(categoriaId));
	}

	@Override
	@PostMapping("/{id}/recomendaciones")
	public ResponseEntity<Void> recomendar(@PathVariable String id, @RequestBody RecomendarProductoDTO dto) {
		recomendarProductoPuertoEntrada.recomendar(id, dto);
		return ResponseEntity.noContent().build();
	}

	@Override
	@GetMapping("/{id}/recomendaciones")
	public ResponseEntity<List<ProductoDTO>> buscarRecomendados(@PathVariable String id) {
		return ResponseEntity.ok(buscarProductosRecomendadosPuertoEntrada.buscarRecomendados(id));
	}
}
