package com.javacadabra.tienda.catalogo.dominio.excepcion;

public class ProductoNoEncontradoExcepcion extends RuntimeException {

	public ProductoNoEncontradoExcepcion(String id) {
		super("No se ha encontrado el producto con id: " + id);
	}
}
