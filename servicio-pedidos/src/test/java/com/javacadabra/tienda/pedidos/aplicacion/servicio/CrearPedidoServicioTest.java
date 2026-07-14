package com.javacadabra.tienda.pedidos.aplicacion.servicio;

import com.javacadabra.tienda.pedidos.aplicacion.dto.entrada.CrearPedidoDTO;
import com.javacadabra.tienda.pedidos.aplicacion.dto.entrada.LineaPedidoDTO;
import com.javacadabra.tienda.pedidos.aplicacion.mapper.PedidoMapper;
import com.javacadabra.tienda.pedidos.aplicacion.puerto.salida.CatalogoPuertoSalida;
import com.javacadabra.tienda.pedidos.aplicacion.puerto.salida.OutboxPuertoSalida;
import com.javacadabra.tienda.pedidos.aplicacion.puerto.salida.PedidoRepositorioPuertoSalida;
import com.javacadabra.tienda.pedidos.aplicacion.puerto.salida.ProductoCatalogoDTO;
import com.javacadabra.tienda.pedidos.dominio.evento.PedidoCreadoEvento;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.ProductoId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrearPedidoServicioTest {

	@Mock
	private PedidoRepositorioPuertoSalida pedidoRepositorioPuertoSalida;

	@Mock
	private CatalogoPuertoSalida catalogoPuertoSalida;

	@Mock
	private OutboxPuertoSalida outboxPuertoSalida;

	private final PedidoMapper pedidoMapper = new PedidoMapper() {
	};

	private final TransactionTemplate transactionTemplate = transactionTemplateQueEjecutaEnElMismoHilo();

	@Test
	void crearUnPedidoConLineasLoGuardaConElTotalCalculado() {
		when(pedidoRepositorioPuertoSalida.guardar(any())).thenAnswer(invocacion -> invocacion.getArgument(0));
		String productoId1 = UUID.randomUUID().toString();
		String productoId2 = UUID.randomUUID().toString();
		when(catalogoPuertoSalida.buscarProductoPorId(any())).thenAnswer(invocacion -> {
			String id = invocacion.<ProductoId>getArgument(0).valor();
			return id.equals(productoId1)
					? new ProductoCatalogoDTO(productoId1, "Producto 1", new BigDecimal("10.00"))
					: new ProductoCatalogoDTO(productoId2, "Producto 2", new BigDecimal("5.50"));
		});

		CrearPedidoServicio servicio = new CrearPedidoServicio(
				pedidoRepositorioPuertoSalida, catalogoPuertoSalida, outboxPuertoSalida, pedidoMapper, transactionTemplate);
		CrearPedidoDTO dto = new CrearPedidoDTO(UUID.randomUUID().toString(), List.of(
				new LineaPedidoDTO(productoId1, 2),
				new LineaPedidoDTO(productoId2, 1)));

		var pedidoCreado = servicio.crear(dto);

		verify(pedidoRepositorioPuertoSalida).guardar(any());
		verify(outboxPuertoSalida).guardar(any(PedidoCreadoEvento.class));
		assertThat(pedidoCreado.clienteId()).isEqualTo(dto.clienteId());
		assertThat(pedidoCreado.lineas()).hasSize(2);
		assertThat(pedidoCreado.total()).isEqualByComparingTo("25.50");
	}

	@Test
	void crearUnPedidoSinLineasLoGuardaConTotalCero() {
		when(pedidoRepositorioPuertoSalida.guardar(any())).thenAnswer(invocacion -> invocacion.getArgument(0));

		CrearPedidoServicio servicio = new CrearPedidoServicio(
				pedidoRepositorioPuertoSalida, catalogoPuertoSalida, outboxPuertoSalida, pedidoMapper, transactionTemplate);
		CrearPedidoDTO dto = new CrearPedidoDTO(UUID.randomUUID().toString(), List.of());

		var pedidoCreado = servicio.crear(dto);

		assertThat(pedidoCreado.lineas()).isEmpty();
		assertThat(pedidoCreado.total()).isEqualByComparingTo(BigDecimal.ZERO);
	}

	private static TransactionTemplate transactionTemplateQueEjecutaEnElMismoHilo() {
		TransactionTemplate transactionTemplate = mock(TransactionTemplate.class);
		when(transactionTemplate.execute(any())).thenAnswer(invocacion -> {
			TransactionCallback<?> callback = invocacion.getArgument(0);
			return callback.doInTransaction(null);
		});
		return transactionTemplate;
	}
}
