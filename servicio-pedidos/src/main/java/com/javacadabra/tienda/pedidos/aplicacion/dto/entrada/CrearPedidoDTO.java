package com.javacadabra.tienda.pedidos.aplicacion.dto.entrada;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record CrearPedidoDTO(
		@Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") String clienteId,
		List<LineaPedidoDTO> lineas) {
}
