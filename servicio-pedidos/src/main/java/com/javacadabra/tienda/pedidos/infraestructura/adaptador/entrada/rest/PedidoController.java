package com.javacadabra.tienda.pedidos.infraestructura.adaptador.entrada.rest;

import com.javacadabra.tienda.pedidos.aplicacion.dto.entrada.CrearPedidoDTO;
import com.javacadabra.tienda.pedidos.aplicacion.dto.salida.PedidoDTO;
import com.javacadabra.tienda.pedidos.aplicacion.puerto.entrada.CrearPedidoPuertoEntrada;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

	private final CrearPedidoPuertoEntrada crearPedidoPuertoEntrada;

	@PostMapping
	@Operation(operationId = "crearPedido", summary = "Crea un pedido para un cliente")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Pedido creado"),
			@ApiResponse(responseCode = "400", description = "Datos de pedido inválidos",
					content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
	})
	public ResponseEntity<PedidoDTO> crear(@RequestBody CrearPedidoDTO dto) {
		PedidoDTO creado = crearPedidoPuertoEntrada.crear(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(creado);
	}
}
