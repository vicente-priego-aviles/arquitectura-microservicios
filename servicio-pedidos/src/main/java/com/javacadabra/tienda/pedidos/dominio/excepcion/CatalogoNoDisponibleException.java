package com.javacadabra.tienda.pedidos.dominio.excepcion;

import lombok.Getter;

@Getter
public class CatalogoNoDisponibleException extends RuntimeException {

	private final String productoId;

	public CatalogoNoDisponibleException(String productoId) {
		super("El catálogo no está disponible para resolver el producto con id: " + productoId);
		this.productoId = productoId;
	}
}
