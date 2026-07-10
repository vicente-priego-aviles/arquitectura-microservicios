package com.javacadabra.tienda.catalogo.infraestructura.adaptador.entrada.rest;

import com.javacadabra.tienda.catalogo.aplicacion.dto.entrada.CrearCategoriaDTO;
import com.javacadabra.tienda.catalogo.aplicacion.dto.salida.CategoriaDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/categorias")
public interface CategoriaControllerApi {

	@PostMapping
	@Operation(operationId = "crearCategoria", summary = "Crea una categoría")
	@ApiResponse(responseCode = "201", description = "Categoría creada")
	ResponseEntity<CategoriaDTO> crear(@RequestBody CrearCategoriaDTO dto);

	@GetMapping("/{id}")
	@Operation(operationId = "buscarCategoriaPorId", summary = "Busca una categoría por id")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Categoría encontrada"),
			@ApiResponse(responseCode = "404", description = "No existe una categoría con ese id",
					content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	})
	ResponseEntity<CategoriaDTO> buscarPorId(@PathVariable String id);
}
