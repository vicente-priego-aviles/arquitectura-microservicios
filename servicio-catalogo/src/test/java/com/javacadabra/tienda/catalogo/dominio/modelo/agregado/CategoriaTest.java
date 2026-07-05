package com.javacadabra.tienda.catalogo.dominio.modelo.agregado;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CategoriaTest {

	@Test
	void crearUnaCategoriaValidaGeneraUnIdYConservaElNombre() {
		Categoria categoria = Categoria.crear("Ropa");

		assertThat(categoria.id()).isNotNull();
		assertThat(categoria.nombre()).isEqualTo("Ropa");
	}

	@Test
	void crearUnaCategoriaConNombreVacioLanzaExcepcion() {
		assertThatThrownBy(() -> Categoria.crear(" "))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void dosCategoriasConElMismoIdSonIguales() {
		Categoria categoria = Categoria.crear("Ropa");
		Categoria reconstruida = Categoria.reconstruir(categoria.id(), "Otro nombre");

		assertThat(categoria).isEqualTo(reconstruida);
	}
}
