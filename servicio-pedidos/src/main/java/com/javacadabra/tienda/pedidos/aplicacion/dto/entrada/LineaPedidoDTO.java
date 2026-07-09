package com.javacadabra.tienda.pedidos.aplicacion.dto.entrada;

import java.math.BigDecimal;

public record LineaPedidoDTO(String productoId, int cantidad, BigDecimal precioUnitario) {
}
