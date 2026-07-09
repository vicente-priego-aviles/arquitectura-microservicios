package com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CantidadTest {

	@Test
	void dosCantidadesConElMismoValorSonIguales() {
		assertThat(Cantidad.de(3)).isEqualTo(Cantidad.de(3));
	}

	@Test
	void unaCantidadCeroLanzaExcepcion() {
		assertThatThrownBy(() -> Cantidad.de(0))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void unaCantidadNegativaLanzaExcepcion() {
		assertThatThrownBy(() -> Cantidad.de(-1))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
