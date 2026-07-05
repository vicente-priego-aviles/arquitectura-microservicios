package com.javacadabra.tienda.catalogo.dominio.excepcion;

public class CategoriaNoEncontradaExcepcion extends RuntimeException {

	public CategoriaNoEncontradaExcepcion(String id) {
		super("No se ha encontrado la categoría con id: " + id);
	}
}
