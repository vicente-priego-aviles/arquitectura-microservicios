package com.javacadabra.tienda.pedidos.dominio.excepcion;

import lombok.Getter;

@Getter
public class ProductoInexistenteException extends RuntimeException {

	private final String id;

	public ProductoInexistenteException(String id) {
		super("No existe un producto en el catálogo con id: " + id);
		this.id = id;
	}
}
