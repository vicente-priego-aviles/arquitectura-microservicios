package com.javacadabra.tienda.inventario.dominio.modelo.objetovalor;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record Cantidad(int valor) {

	public Cantidad {
		if (valor < 0) {
			throw new IllegalArgumentException("La cantidad de stock no puede ser negativa: " + valor);
		}
	}

	public static Cantidad de(int valor) {
		return new Cantidad(valor);
	}
}
