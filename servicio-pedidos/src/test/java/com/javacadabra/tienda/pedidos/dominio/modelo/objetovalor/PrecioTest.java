package com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrecioTest {

	@Test
	void dosPreciosConElMismoValorSonIguales() {
		assertThat(Precio.de(new BigDecimal("19.99"))).isEqualTo(Precio.de(new BigDecimal("19.99")));
	}

	@Test
	void unPrecioNegativoLanzaExcepcion() {
		assertThatThrownBy(() -> Precio.de(new BigDecimal("-0.01")))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void unPrecioNuloLanzaExcepcion() {
		assertThatThrownBy(() -> Precio.de(null))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
