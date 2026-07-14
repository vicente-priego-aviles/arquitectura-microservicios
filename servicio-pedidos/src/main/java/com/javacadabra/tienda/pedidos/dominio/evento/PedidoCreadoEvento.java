package com.javacadabra.tienda.pedidos.dominio.evento;

import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.Cantidad;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.PedidoId;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.ProductoId;

import java.time.Instant;
import java.util.List;

public record PedidoCreadoEvento(PedidoId pedidoId, List<LineaPedidoCreada> lineas, Instant ocurridoEn) {

	public record LineaPedidoCreada(ProductoId productoId, Cantidad cantidad) {
	}
}
