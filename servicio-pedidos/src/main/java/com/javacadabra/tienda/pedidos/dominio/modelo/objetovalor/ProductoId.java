package com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor;

import java.util.UUID;

public record ProductoId(String valor) {

	public ProductoId {
		if (valor == null || valor.isBlank()) {
			throw new IllegalArgumentException("El id del producto no puede estar vacío");
		}
		try {
			UUID.fromString(valor);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("El id del producto debe ser un UUID válido: " + valor, e);
		}
	}

	public static ProductoId de(String valor) {
		return new ProductoId(valor);
	}
}
