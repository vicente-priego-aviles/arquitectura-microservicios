package com.javacadabra.tienda.catalogo.dominio.excepcion;

public class ProductoNoEncontradoException extends RuntimeException {

	public ProductoNoEncontradoException(String id) {
		super("No se ha encontrado el producto con id: " + id);
	}
}
