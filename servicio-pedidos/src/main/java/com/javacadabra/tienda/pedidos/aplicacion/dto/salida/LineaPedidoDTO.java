package com.javacadabra.tienda.pedidos.aplicacion.dto.salida;

import java.math.BigDecimal;

public record LineaPedidoDTO(String productoId, int cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {
}
