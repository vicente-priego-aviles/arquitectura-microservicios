package com.javacadabra.tienda.pedidos.dominio.comando;

import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.Cantidad;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.PedidoId;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.ProductoId;

import java.util.List;

public record ReservarStockComando(PedidoId pedidoId, List<LineaReserva> lineas) {

	public record LineaReserva(ProductoId productoId, Cantidad cantidad) {
	}
}
