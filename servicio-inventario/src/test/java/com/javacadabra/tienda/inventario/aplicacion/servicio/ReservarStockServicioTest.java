package com.javacadabra.tienda.inventario.aplicacion.servicio;

import com.javacadabra.tienda.inventario.aplicacion.dto.entrada.LineaReservaDTO;
import com.javacadabra.tienda.inventario.aplicacion.puerto.salida.OutboxPuertoSalida;
import com.javacadabra.tienda.inventario.aplicacion.puerto.salida.PedidoProcesadoPuertoSalida;
import com.javacadabra.tienda.inventario.aplicacion.puerto.salida.StockRepositorioPuertoSalida;
import com.javacadabra.tienda.inventario.dominio.evento.ReservaStockRechazadaEvento;
import com.javacadabra.tienda.inventario.dominio.evento.StockReservadoEvento;
import com.javacadabra.tienda.inventario.dominio.modelo.agregado.Stock;
import com.javacadabra.tienda.inventario.dominio.modelo.objetovalor.Cantidad;
import com.javacadabra.tienda.inventario.dominio.modelo.objetovalor.ProductoId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservarStockServicioTest {

	@Mock
	private StockRepositorioPuertoSalida stockRepositorioPuertoSalida;

	@Mock
	private PedidoProcesadoPuertoSalida pedidoProcesadoPuertoSalida;

	@Mock
	private OutboxPuertoSalida outboxPuertoSalida;

	private final TransactionTemplate transactionTemplate = transactionTemplateQueEjecutaEnElMismoHilo();

	@Test
	void reservarDecrementaElStockDeCadaLineaYPublicaStockReservado() {
		String productoId = UUID.randomUUID().toString();
		when(pedidoProcesadoPuertoSalida.yaProcesado(any())).thenReturn(false);
		when(stockRepositorioPuertoSalida.buscarPorProductoId(any()))
				.thenReturn(Optional.of(Stock.crear(ProductoId.de(productoId), Cantidad.de(10))));

		var servicio = new ReservarStockServicio(
				stockRepositorioPuertoSalida, pedidoProcesadoPuertoSalida, outboxPuertoSalida, transactionTemplate);
		String pedidoId = UUID.randomUUID().toString();
		servicio.reservar(pedidoId, List.of(new LineaReservaDTO(productoId, 4)));

		verify(stockRepositorioPuertoSalida).guardar(any());
		verify(pedidoProcesadoPuertoSalida).marcarProcesado(pedidoId);
		verify(outboxPuertoSalida).guardar(any(StockReservadoEvento.class));
	}

	@Test
	void reservarUnPedidoYaProcesadoNoTocaElStock() {
		when(pedidoProcesadoPuertoSalida.yaProcesado(any())).thenReturn(true);

		var servicio = new ReservarStockServicio(
				stockRepositorioPuertoSalida, pedidoProcesadoPuertoSalida, outboxPuertoSalida, transactionTemplate);
		servicio.reservar(UUID.randomUUID().toString(), List.of(new LineaReservaDTO(UUID.randomUUID().toString(), 1)));

		verify(stockRepositorioPuertoSalida, never()).guardar(any());
		verify(pedidoProcesadoPuertoSalida, never()).marcarProcesado(any());
		verify(outboxPuertoSalida, never()).guardar(any(StockReservadoEvento.class));
	}

	@Test
	void reservarMasCantidadDeLaDisponiblePublicaReservaStockRechazadaEnVezDeLanzarExcepcion() {
		String productoId = UUID.randomUUID().toString();
		when(pedidoProcesadoPuertoSalida.yaProcesado(any())).thenReturn(false);
		when(stockRepositorioPuertoSalida.buscarPorProductoId(any()))
				.thenReturn(Optional.of(Stock.crear(ProductoId.de(productoId), Cantidad.de(2))));

		var servicio = new ReservarStockServicio(
				stockRepositorioPuertoSalida, pedidoProcesadoPuertoSalida, outboxPuertoSalida, transactionTemplate);
		String pedidoId = UUID.randomUUID().toString();

		servicio.reservar(pedidoId, List.of(new LineaReservaDTO(productoId, 5)));

		verify(stockRepositorioPuertoSalida, never()).guardar(any());
		verify(pedidoProcesadoPuertoSalida).marcarProcesado(pedidoId);
		verify(outboxPuertoSalida).guardar(any(ReservaStockRechazadaEvento.class));
	}

	private static TransactionTemplate transactionTemplateQueEjecutaEnElMismoHilo() {
		TransactionTemplate transactionTemplate = mock(TransactionTemplate.class);
		doAnswer(invocacion -> {
			Consumer<TransactionStatus> callback = invocacion.getArgument(0);
			callback.accept(null);
			return null;
		}).when(transactionTemplate).executeWithoutResult(any());
		return transactionTemplate;
	}
}
