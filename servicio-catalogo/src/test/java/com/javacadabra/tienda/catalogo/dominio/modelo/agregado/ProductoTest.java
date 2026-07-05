package com.javacadabra.tienda.catalogo.dominio.modelo.agregado;

import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.CategoriaId;
import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.Precio;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductoTest {

	@Test
	void crearUnProductoValidoGeneraUnIdYConservaLosDatos() {
		Producto producto = Producto.crear("Camiseta", "100% algodón", Precio.de(new BigDecimal("19.99")), CategoriaId.generar());

		assertThat(producto.id()).isNotNull();
		assertThat(producto.nombre()).isEqualTo("Camiseta");
		assertThat(producto.precio().valor()).isEqualByComparingTo("19.99");
		assertThat(producto.categoriaId()).isNotNull();
	}

	@Test
	void crearUnProductoConNombreVacioLanzaExcepcion() {
		assertThatThrownBy(() -> Producto.crear(" ", "descripcion", Precio.de(BigDecimal.TEN), CategoriaId.generar()))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void crearUnProductoSinCategoriaLanzaExcepcion() {
		assertThatThrownBy(() -> Producto.crear("Camiseta", "descripcion", Precio.de(BigDecimal.TEN), null))
				.isInstanceOf(NullPointerException.class);
	}

	@Test
	void dosProductosConElMismoIdSonIguales() {
		Producto producto = Producto.crear("Camiseta", "100% algodón", Precio.de(BigDecimal.TEN), CategoriaId.generar());
		Producto reconstruido = Producto.reconstruir(producto.id(), "Otro nombre", "Otra descripción", Precio.de(BigDecimal.ONE), producto.categoriaId(), producto.fechaCreacion());

		assertThat(producto).isEqualTo(reconstruido);
	}

	@Test
	void unProductoNoPuedeRecomendarseASiMismo() {
		Producto producto = Producto.crear("Camiseta", "100% algodón", Precio.de(BigDecimal.TEN), CategoriaId.generar());

		assertThatThrownBy(() -> producto.validarRecomendacion(producto.id()))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void unProductoPuedeValidarLaRecomendacionDeOtroProductoDistinto() {
		Producto producto = Producto.crear("Camiseta", "100% algodón", Precio.de(BigDecimal.TEN), CategoriaId.generar());
		Producto otroProducto = Producto.crear("Pantalón", "Vaquero", Precio.de(BigDecimal.TEN), CategoriaId.generar());

		assertThatCode(() -> producto.validarRecomendacion(otroProducto.id())).doesNotThrowAnyException();
	}
}
