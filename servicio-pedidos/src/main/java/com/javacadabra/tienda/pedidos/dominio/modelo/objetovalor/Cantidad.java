package com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor;

public record Cantidad(int valor) {

	public Cantidad {
		if (valor <= 0) {
			throw new IllegalArgumentException("La cantidad debe ser mayor que cero: " + valor);
		}
	}

	public static Cantidad de(int valor) {
		return new Cantidad(valor);
	}
}
