package com.javacadabra.tienda.catalogo.dominio.excepcion;

public class CategoriaNoEncontradaException extends RuntimeException {

	public CategoriaNoEncontradaException(String id) {
		super("No se ha encontrado la categoría con id: " + id);
	}
}
