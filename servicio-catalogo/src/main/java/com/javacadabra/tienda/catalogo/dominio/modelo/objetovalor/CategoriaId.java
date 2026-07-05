package com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor;

import java.util.UUID;

public record CategoriaId(String valor) {

	public CategoriaId {
		if (valor == null || valor.isBlank()) {
			throw new IllegalArgumentException("El id de la categoría no puede estar vacío");
		}
		try {
			UUID.fromString(valor);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("El id de la categoría debe ser un UUID válido: " + valor, e);
		}
	}

	public static CategoriaId generar() {
		return new CategoriaId(UUID.randomUUID().toString());
	}

	public static CategoriaId de(String valor) {
		return new CategoriaId(valor);
	}
}
