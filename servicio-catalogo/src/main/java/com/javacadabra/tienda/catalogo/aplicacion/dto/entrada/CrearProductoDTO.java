package com.javacadabra.tienda.catalogo.aplicacion.dto.entrada;

import java.math.BigDecimal;

public record CrearProductoDTO(String nombre, String descripcion, BigDecimal precio, String categoriaId) {
}
