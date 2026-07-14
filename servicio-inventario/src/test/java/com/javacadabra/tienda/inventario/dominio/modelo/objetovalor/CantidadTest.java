package com.javacadabra.tienda.inventario.dominio.modelo.objetovalor;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CantidadTest {

	@Test
	void dosCantidadesConElMismoValorSonIguales() {
		assertThat(Cantidad.de(3)).isEqualTo(Cantidad.de(3));
	}

	@Test
	void unaCantidadCeroEsValida() {
		assertThat(Cantidad.de(0).valor()).isZero();
	}

	@Test
	void unaCantidadNegativaLanzaExcepcion() {
		assertThatThrownBy(() -> Cantidad.de(-1))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
