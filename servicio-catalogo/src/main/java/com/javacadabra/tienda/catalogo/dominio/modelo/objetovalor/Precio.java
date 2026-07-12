package com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor;

import org.jmolecules.ddd.annotation.ValueObject;

import java.math.BigDecimal;

@ValueObject
public record Precio(BigDecimal valor) {

	public Precio {
		if (valor == null) {
			throw new IllegalArgumentException("El precio no puede ser nulo");
		}
		if (valor.compareTo(BigDecimal.ZERO) < 0) {
			throw new IllegalArgumentException("El precio no puede ser negativo: " + valor);
		}
	}

	public static Precio de(BigDecimal valor) {
		return new Precio(valor);
	}
}
