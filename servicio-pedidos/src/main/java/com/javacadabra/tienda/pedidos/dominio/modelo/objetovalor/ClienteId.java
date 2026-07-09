package com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor;

import java.util.UUID;

public record ClienteId(String valor) {

	public ClienteId {
		if (valor == null || valor.isBlank()) {
			throw new IllegalArgumentException("El id del cliente no puede estar vacío");
		}
		try {
			UUID.fromString(valor);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("El id del cliente debe ser un UUID válido: " + valor, e);
		}
	}

	public static ClienteId de(String valor) {
		return new ClienteId(valor);
	}
}
