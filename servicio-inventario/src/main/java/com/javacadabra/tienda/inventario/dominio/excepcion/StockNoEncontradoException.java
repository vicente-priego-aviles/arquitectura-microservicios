package com.javacadabra.tienda.inventario.dominio.excepcion;

import com.javacadabra.tienda.inventario.dominio.modelo.objetovalor.ProductoId;
import lombok.Getter;

@Getter
public class StockNoEncontradoException extends RuntimeException {

	private final ProductoId productoId;

	public StockNoEncontradoException(ProductoId productoId) {
		super("No existe stock registrado para el producto " + productoId.valor());
		this.productoId = productoId;
	}
}
