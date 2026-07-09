package com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor;

import java.util.UUID;

public record PedidoId(String valor) {

	public PedidoId {
		if (valor == null || valor.isBlank()) {
			throw new IllegalArgumentException("El id del pedido no puede estar vacío");
		}
		try {
			UUID.fromString(valor);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("El id del pedido debe ser un UUID válido: " + valor, e);
		}
	}

	public static PedidoId generar() {
		return new PedidoId(UUID.randomUUID().toString());
	}

	public static PedidoId de(String valor) {
		return new PedidoId(valor);
	}
}
