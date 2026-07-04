package com.javacadabra.tienda.catalogo.dominio.modelo.agregado;

import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.Precio;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductoTest {

	@Test
	void crearUnProductoValidoGeneraUnIdYConservaLosDatos() {
		Producto producto = Producto.crear("Camiseta", "100% algodón", Precio.de(new BigDecimal("19.99")));

		assertThat(producto.id()).isNotNull();
		assertThat(producto.nombre()).isEqualTo("Camiseta");
		assertThat(producto.precio().valor()).isEqualByComparingTo("19.99");
	}

	@Test
	void crearUnProductoConNombreVacioLanzaExcepcion() {
		assertThatThrownBy(() -> Producto.crear(" ", "descripcion", Precio.de(BigDecimal.TEN)))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void dosProductosConElMismoIdSonIguales() {
		Producto producto = Producto.crear("Camiseta", "100% algodón", Precio.de(BigDecimal.TEN));
		Producto reconstruido = Producto.reconstruir(producto.id(), "Otro nombre", "Otra descripción", Precio.de(BigDecimal.ONE), producto.fechaCreacion());

		assertThat(producto).isEqualTo(reconstruido);
	}
}
