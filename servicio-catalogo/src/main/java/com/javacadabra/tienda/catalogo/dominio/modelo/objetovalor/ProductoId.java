package com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor;

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

	public static ProductoId generar() {
		return new ProductoId(UUID.randomUUID().toString());
	}

	public static ProductoId de(String valor) {
		return new ProductoId(valor);
	}
}
