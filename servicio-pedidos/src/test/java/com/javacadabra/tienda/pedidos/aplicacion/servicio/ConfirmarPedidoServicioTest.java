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
class ConfirmarPedidoServicioTest {

	@Mock
	private PedidoRepositorioPuertoSalida pedidoRepositorioPuertoSalida;

	@Test
	void confirmarUnPedidoExistenteLoDejaConfirmado() {
		Pedido pedido = Pedido.crear(ClienteId.de(UUID.randomUUID().toString()));
		when(pedidoRepositorioPuertoSalida.buscarPorId(pedido.id())).thenReturn(Optional.of(pedido));

		new ConfirmarPedidoServicio(pedidoRepositorioPuertoSalida).confirmar(pedido.id().valor());

		ArgumentCaptor<Pedido> captor = ArgumentCaptor.forClass(Pedido.class);
		verify(pedidoRepositorioPuertoSalida).guardar(captor.capture());
		assertThat(captor.getValue().estado()).isEqualTo(EstadoPedido.CONFIRMADO);
	}

	@Test
	void confirmarUnPedidoInexistenteLanzaExcepcion() {
		when(pedidoRepositorioPuertoSalida.buscarPorId(any())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> new ConfirmarPedidoServicio(pedidoRepositorioPuertoSalida)
				.confirmar(UUID.randomUUID().toString()))
				.isInstanceOf(PedidoNoEncontradoException.class);
	}
}
