package com.javacadabra.tienda.catalogo.dominio.excepcion;

import lombok.Getter;

@Getter
public class ProductoNoEncontradoException extends RuntimeException {

	private final String id;

	public ProductoNoEncontradoException(String id) {
		super("No se ha encontrado el producto con id: " + id);
		this.id = id;
	}
}
