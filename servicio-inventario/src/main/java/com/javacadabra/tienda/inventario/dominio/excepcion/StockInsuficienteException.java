package com.javacadabra.tienda.inventario.dominio.excepcion;

import com.javacadabra.tienda.inventario.dominio.modelo.objetovalor.ProductoId;

public class StockInsuficienteException extends RuntimeException {

	public StockInsuficienteException(ProductoId productoId, int disponible, int solicitado) {
		super("Stock insuficiente para el producto %s: disponible %d, solicitado %d"
				.formatted(productoId.valor(), disponible, solicitado));
	}
}
