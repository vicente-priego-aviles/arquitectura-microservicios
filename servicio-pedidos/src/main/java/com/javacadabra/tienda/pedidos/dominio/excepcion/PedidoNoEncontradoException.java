package com.javacadabra.tienda.pedidos.dominio.excepcion;

import lombok.Getter;

@Getter
public class PedidoNoEncontradoException extends RuntimeException {

	private final String id;

	public PedidoNoEncontradoException(String id) {
		super("No existe un pedido con id: " + id);
		this.id = id;
	}
}
