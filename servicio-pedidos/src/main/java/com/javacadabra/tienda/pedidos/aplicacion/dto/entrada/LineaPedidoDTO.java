package com.javacadabra.tienda.pedidos.aplicacion.dto.entrada;

import io.swagger.v3.oas.annotations.media.Schema;

public record LineaPedidoDTO(
		@Schema(description = "Id de un producto ya existente en el catálogo", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") String productoId,
		@Schema(example = "2") int cantidad) {
}
