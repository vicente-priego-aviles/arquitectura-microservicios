package com.javacadabra.tienda.catalogo.infraestructura.adaptador.entrada.rest;

import com.javacadabra.tienda.catalogo.aplicacion.dto.entrada.CrearProductoDTO;
import com.javacadabra.tienda.catalogo.aplicacion.dto.entrada.RecomendarProductoDTO;
import com.javacadabra.tienda.catalogo.aplicacion.dto.salida.ProductoDTO;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada.BuscarProductoPuertoEntrada;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada.BuscarProductosPorCategoriaPuertoEntrada;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada.BuscarProductosRecomendadosPuertoEntrada;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada.CrearProductoPuertoEntrada;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada.RecomendarProductoPuertoEntrada;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
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
public class ProductoController {

	private final CrearProductoPuertoEntrada crearProductoPuertoEntrada;
	private final BuscarProductoPuertoEntrada buscarProductoPuertoEntrada;
	private final BuscarProductosPorCategoriaPuertoEntrada buscarProductosPorCategoriaPuertoEntrada;
	private final RecomendarProductoPuertoEntrada recomendarProductoPuertoEntrada;
	private final BuscarProductosRecomendadosPuertoEntrada buscarProductosRecomendadosPuertoEntrada;

	@PostMapping
	@Operation(operationId = "crearProducto", summary = "Crea un producto en una categoría existente")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Producto creado"),
			@ApiResponse(responseCode = "404", description = "La categoría indicada no existe",
					content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	})
	public ResponseEntity<ProductoDTO> crear(@RequestBody CrearProductoDTO dto) {
		ProductoDTO creado = crearProductoPuertoEntrada.crear(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(creado);
	}

	@GetMapping("/{id}")
	@Operation(operationId = "buscarProductoPorId", summary = "Busca un producto por id")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Producto encontrado"),
			@ApiResponse(responseCode = "404", description = "No existe un producto con ese id",
					content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	})
	public ResponseEntity<ProductoDTO> buscarPorId(@PathVariable String id) {
		return ResponseEntity.ok(buscarProductoPuertoEntrada.buscarPorId(id));
	}

	@GetMapping(params = "categoriaId")
	@Operation(summary = "Lista los productos de una categoría")
	public ResponseEntity<List<ProductoDTO>> buscarPorCategoria(@RequestParam String categoriaId) {
		return ResponseEntity.ok(buscarProductosPorCategoriaPuertoEntrada.buscarPorCategoria(categoriaId));
	}

	@PostMapping("/{id}/recomendaciones")
	@Operation(summary = "Añade una recomendación de producto")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Recomendación añadida", content = @Content),
			@ApiResponse(responseCode = "400", description = "Un producto no puede recomendarse a sí mismo",
					content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
			@ApiResponse(responseCode = "404", description = "El producto o el producto recomendado no existen",
					content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	})
	public ResponseEntity<Void> recomendar(@PathVariable String id, @RequestBody RecomendarProductoDTO dto) {
		recomendarProductoPuertoEntrada.recomendar(id, dto);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{id}/recomendaciones")
	@Operation(summary = "Lista los productos recomendados a partir de este")
	public ResponseEntity<List<ProductoDTO>> buscarRecomendados(@PathVariable String id) {
		return ResponseEntity.ok(buscarProductosRecomendadosPuertoEntrada.buscarRecomendados(id));
	}
}
