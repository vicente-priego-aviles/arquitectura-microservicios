package com.javacadabra.tienda.pedidos.aplicacion.dto.salida;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PedidoDTO(String id, String clienteId, List<LineaPedidoDTO> lineas, BigDecimal total, Instant fechaCreacion) {
}
