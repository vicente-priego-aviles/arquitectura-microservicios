package com.javacadabra.tienda.catalogo.infraestructura.adaptador.entrada.rest;

import com.javacadabra.tienda.catalogo.aplicacion.dto.entrada.CrearProductoDTO;
import com.javacadabra.tienda.catalogo.aplicacion.dto.entrada.RecomendarProductoDTO;
import com.javacadabra.tienda.catalogo.aplicacion.dto.salida.ProductoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ProductoApiDoc {

	@Operation(operationId = "crearProducto", summary = "Crea un producto en una categoría existente")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Producto creado"),
			@ApiResponse(responseCode = "404", description = "La categoría indicada no existe",
					content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	})
	ResponseEntity<ProductoDTO> crear(CrearProductoDTO dto);

	@Operation(operationId = "buscarProductoPorId", summary = "Busca un producto por id")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Producto encontrado"),
			@ApiResponse(responseCode = "404", description = "No existe un producto con ese id",
					content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	})
	ResponseEntity<ProductoDTO> buscarPorId(String id);

	@Operation(summary = "Lista los productos de una categoría")
	ResponseEntity<List<ProductoDTO>> buscarPorCategoria(String categoriaId);

	@Operation(summary = "Añade una recomendación de producto")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Recomendación añadida", content = @Content),
			@ApiResponse(responseCode = "400", description = "Un producto no puede recomendarse a sí mismo",
					content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
			@ApiResponse(responseCode = "404", description = "El producto o el producto recomendado no existen",
					content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	})
	ResponseEntity<Void> recomendar(String id, RecomendarProductoDTO dto);

	@Operation(summary = "Lista los productos recomendados a partir de este")
	ResponseEntity<List<ProductoDTO>> buscarRecomendados(String id);
}
