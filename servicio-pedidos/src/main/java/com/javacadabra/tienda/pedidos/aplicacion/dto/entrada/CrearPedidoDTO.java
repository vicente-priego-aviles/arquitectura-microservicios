package com.javacadabra.tienda.pedidos.aplicacion.dto.entrada;

import java.util.List;

public record CrearPedidoDTO(String clienteId, List<LineaPedidoDTO> lineas) {
}
