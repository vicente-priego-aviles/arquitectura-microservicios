package com.javacadabra.tienda.catalogo.aplicacion.dto.salida;

import java.math.BigDecimal;

public record ProductoDTO(String id, String nombre, String descripcion, BigDecimal precio, String categoriaId) {
}
