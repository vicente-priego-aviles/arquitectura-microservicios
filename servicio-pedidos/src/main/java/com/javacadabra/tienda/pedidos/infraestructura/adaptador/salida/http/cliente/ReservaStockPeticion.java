package com.javacadabra.tienda.pedidos.infraestructura.adaptador.salida.http.cliente;

import java.util.List;

public record ReservaStockPeticion(String pedidoId, List<LineaReservaPeticion> lineas) {

	public record LineaReservaPeticion(String productoId, int cantidad) {
	}
}
