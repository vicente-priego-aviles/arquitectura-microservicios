package com.javacadabra.tienda.pedidos.aplicacion.servicio;

import com.javacadabra.tienda.pedidos.aplicacion.puerto.salida.PedidoRepositorioPuertoSalida;
import com.javacadabra.tienda.pedidos.dominio.excepcion.PedidoNoEncontradoException;
import com.javacadabra.tienda.pedidos.dominio.modelo.agregado.Pedido;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.ClienteId;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.EstadoPedido;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelarPedidoServicioTest {

	@Mock
	private PedidoRepositorioPuertoSalida pedidoRepositorioPuertoSalida;

	@Test
	void cancelarUnPedidoExistenteLoDejaCanceladoConElMotivo() {
		Pedido pedido = Pedido.crear(ClienteId.de(UUID.randomUUID().toString()));
		when(pedidoRepositorioPuertoSalida.buscarPorId(pedido.id())).thenReturn(Optional.of(pedido));

		new CancelarPedidoServicio(pedidoRepositorioPuertoSalida)
				.cancelar(pedido.id().valor(), "Stock insuficiente para el producto X");

		ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
		verify(pedidoRepositorioPuertoSalida).guardar(captor.capture());
		assertThat(captor.getValue().estado()).isEqualTo(EstadoPedido.CANCELADO);
		assertThat(captor.getValue().motivoCancelacion()).isEqualTo("Stock insuficiente para el producto X");
	}

	@Test
	void cancelarUnPedidoInexistenteLanzaExcepcion() {
		when(pedidoRepositorioPuertoSalida.buscarPorId(any())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> new CancelarPedidoServicio(pedidoRepositorioPuertoSalida)
				.cancelar(UUID.randomUUID().toString(), "motivo"))
				.isInstanceOf(PedidoNoEncontradoException.class);
	}
}
