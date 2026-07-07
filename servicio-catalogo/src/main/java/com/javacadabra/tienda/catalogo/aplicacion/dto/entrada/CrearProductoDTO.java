package com.javacadabra.tienda.catalogo.aplicacion.dto.entrada;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record CrearProductoDTO(
		@Schema(example = "Camiseta") String nombre,
		@Schema(example = "100% algodón") String descripcion,
		@Schema(example = "19.99") BigDecimal precio,
		@Schema(description = "Id de una categoría ya existente", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") String categoriaId) {
}
