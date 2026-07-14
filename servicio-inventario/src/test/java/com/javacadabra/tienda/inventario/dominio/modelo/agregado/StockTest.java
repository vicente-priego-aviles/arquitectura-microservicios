package com.javacadabra.tienda.inventario.dominio.modelo.agregado;

import com.javacadabra.tienda.inventario.dominio.excepcion.StockInsuficienteException;
import com.javacadabra.tienda.inventario.dominio.modelo.objetovalor.Cantidad;
import com.javacadabra.tienda.inventario.dominio.modelo.objetovalor.ProductoId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StockTest {

	private final ProductoId productoId = ProductoId.de(UUID.randomUUID().toString());

	@Test
	void decrementarRestaLaCantidadDisponible() {
		Stock stock = Stock.crear(productoId, Cantidad.de(10));

		stock.decrementar(4);

		assertThat(stock.cantidad()).isEqualTo(Cantidad.de(6));
	}

	@Test
	void decrementarHastaDejarloEnCeroEsValido() {
		Stock stock = Stock.crear(productoId, Cantidad.de(5));

		stock.decrementar(5);

		assertThat(stock.cantidad()).isEqualTo(Cantidad.de(0));
	}

	@Test
	void decrementarMasDeLoDisponibleLanzaExcepcion() {
		Stock stock = Stock.crear(productoId, Cantidad.de(3));

		assertThatThrownBy(() -> stock.decrementar(4))
				.isInstanceOf(StockInsuficienteException.class);
	}
}
