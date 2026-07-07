package com.javacadabra.tienda.catalogo.aplicacion.dto.entrada;

import io.swagger.v3.oas.annotations.media.Schema;

public record RecomendarProductoDTO(
		@Schema(description = "Id de un producto ya existente, distinto del producto de origen", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") String productoRecomendadoId) {
}
