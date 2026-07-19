package com.javacadabra.tienda.pedidos.dominio.modelo.agregado;

import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.Cantidad;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.ClienteId;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.EstadoPedido;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.Precio;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.ProductoId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PedidoTest {

	@Test
	void crearUnPedidoValidoGeneraUnIdYArrancaSinLineas() {
		Pedido pedido = Pedido.crear(ClienteId.de(UUID.randomUUID().toString()));

		assertThat(pedido.id()).isNotNull();
		assertThat(pedido.clienteId()).isNotNull();
		assertThat(pedido.lineas()).isEmpty();
		assertThat(pedido.estado()).isEqualTo(EstadoPedido.PENDIENTE_CONFIRMACION);
	}

	@Test
	void crearUnPedidoSinClienteLanzaExcepcion() {
		assertThatThrownBy(() -> Pedido.crear(null))
				.isInstanceOf(NullPointerException.class);
	}

	@Test
	void agregarUnaLineaLaIncluyeEnElPedido() {
		Pedido pedido = Pedido.crear(ClienteId.de(UUID.randomUUID().toString()));

		pedido.agregarLinea(ProductoId.de(UUID.randomUUID().toString()), Cantidad.de(2), Precio.de(new BigDecimal("19.99")));

		assertThat(pedido.lineas()).hasSize(1);
	}

	@Test
	void agregarUnaLineaConProductoNuloLanzaExcepcion() {
		Pedido pedido = Pedido.crear(ClienteId.de(UUID.randomUUID().toString()));

		assertThatThrownBy(() -> pedido.agregarLinea(null, Cantidad.de(1), Precio.de(BigDecimal.TEN)))
				.isInstanceOf(NullPointerException.class);
	}

	@Test
	void elTotalSumaElSubtotalDeCadaLinea() {
		Pedido pedido = Pedido.crear(ClienteId.de(UUID.randomUUID().toString()));
		pedido.agregarLinea(ProductoId.de(UUID.randomUUID().toString()), Cantidad.de(2), Precio.de(new BigDecimal("10.00")));
		pedido.agregarLinea(ProductoId.de(UUID.randomUUID().toString()), Cantidad.de(1), Precio.de(new BigDecimal("5.50")));

		assertThat(pedido.total().valor()).isEqualByComparingTo("25.50");
	}

	@Test
	void unPedidoSinLineasTieneTotalCero() {
		Pedido pedido = Pedido.crear(ClienteId.de(UUID.randomUUID().toString()));

		assertThat(pedido.total().valor()).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	void dosPedidosConElMismoIdSonIguales() {
		Pedido pedido = Pedido.crear(ClienteId.de(UUID.randomUUID().toString()));
		Pedido reconstruido = Pedido.reconstruir(pedido.id(), pedido.clienteId(), pedido.lineas(), pedido.fechaCreacion(),
				pedido.estado(), pedido.motivoCancelacion());

		assertThat(pedido).isEqualTo(reconstruido);
	}

	@Test
	void confirmarUnPedidoPendienteLoDejaConfirmado() {
		Pedido pedido = Pedido.crear(ClienteId.de(UUID.randomUUID().toString()));

		pedido.confirmar();

		assertThat(pedido.estado()).isEqualTo(EstadoPedido.CONFIRMADO);
	}

	@Test
	void cancelarUnPedidoPendienteLoDejaCanceladoConElMotivo() {
		Pedido pedido = Pedido.crear(ClienteId.de(UUID.randomUUID().toString()));

		pedido.cancelar("Stock insuficiente");

		assertThat(pedido.estado()).isEqualTo(EstadoPedido.CANCELADO);
		assertThat(pedido.motivoCancelacion()).isEqualTo("Stock insuficiente");
	}

	@Test
	void cancelarUnPedidoYaCanceladoNoSobreescribeElMotivoOriginal() {
		Pedido pedido = Pedido.crear(ClienteId.de(UUID.randomUUID().toString()));

		pedido.cancelar("Stock insuficiente");
		pedido.cancelar("Motivo de una redelivery distinta");

		assertThat(pedido.motivoCancelacion()).isEqualTo("Stock insuficiente");
	}
}
