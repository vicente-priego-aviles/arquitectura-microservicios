package com.javacadabra.tienda.inventario.dominio.excepcion;

import com.javacadabra.tienda.inventario.dominio.modelo.objetovalor.ProductoId;

public class StockNoEncontradoException extends RuntimeException {

	public StockNoEncontradoException(ProductoId productoId) {
		super("No existe stock registrado para el producto " + productoId.valor());
	}
}
