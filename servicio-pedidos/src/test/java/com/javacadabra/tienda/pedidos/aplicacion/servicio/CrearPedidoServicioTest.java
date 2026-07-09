package com.javacadabra.tienda.pedidos.aplicacion.servicio;

import com.javacadabra.tienda.pedidos.aplicacion.dto.entrada.CrearPedidoDTO;
import com.javacadabra.tienda.pedidos.aplicacion.dto.entrada.LineaPedidoDTO;
import com.javacadabra.tienda.pedidos.aplicacion.mapper.PedidoMapper;
import com.javacadabra.tienda.pedidos.aplicacion.puerto.salida.PedidoRepositorioPuertoSalida;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrearPedidoServicioTest {

	@Mock
	private PedidoRepositorioPuertoSalida pedidoRepositorioPuertoSalida;

	private final PedidoMapper pedidoMapper = new PedidoMapper() {
	};

	@Test
	void crearUnPedidoConLineasLoGuardaConElTotalCalculado() {
		when(pedidoRepositorioPuertoSalida.guardar(any())).thenAnswer(invocacion -> invocacion.getArgument(0));

		CrearPedidoServicio servicio = new CrearPedidoServicio(pedidoRepositorioPuertoSalida, pedidoMapper);
		CrearPedidoDTO dto = new CrearPedidoDTO(UUID.randomUUID().toString(), List.of(
				new LineaPedidoDTO(UUID.randomUUID().toString(), 2, new BigDecimal("10.00")),
				new LineaPedidoDTO(UUID.randomUUID().toString(), 1, new BigDecimal("5.50"))));

		var pedidoCreado = servicio.crear(dto);

		verify(pedidoRepositorioPuertoSalida).guardar(any());
		assertThat(pedidoCreado.clienteId()).isEqualTo(dto.clienteId());
		assertThat(pedidoCreado.lineas()).hasSize(2);
		assertThat(pedidoCreado.total()).isEqualByComparingTo("25.50");
	}

	@Test
	void crearUnPedidoSinLineasLoGuardaConTotalCero() {
		when(pedidoRepositorioPuertoSalida.guardar(any())).thenAnswer(invocacion -> invocacion.getArgument(0));

		CrearPedidoServicio servicio = new CrearPedidoServicio(pedidoRepositorioPuertoSalida, pedidoMapper);
		CrearPedidoDTO dto = new CrearPedidoDTO(UUID.randomUUID().toString(), List.of());

		var pedidoCreado = servicio.crear(dto);

		assertThat(pedidoCreado.lineas()).isEmpty();
		assertThat(pedidoCreado.total()).isEqualByComparingTo(BigDecimal.ZERO);
	}
}
