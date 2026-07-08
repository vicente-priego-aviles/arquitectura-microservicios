package com.javacadabra.tienda.catalogo.dominio.excepcion;

import lombok.Getter;

@Getter
public class CategoriaNoEncontradaException extends RuntimeException {

	private final String id;

	public CategoriaNoEncontradaException(String id) {
		super("No se ha encontrado la categoría con id: " + id);
		this.id = id;
	}
}
