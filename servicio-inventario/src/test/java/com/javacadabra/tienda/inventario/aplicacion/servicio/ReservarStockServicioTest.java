package com.javacadabra.tienda.inventario.aplicacion.servicio;

import com.javacadabra.tienda.inventario.aplicacion.dto.entrada.LineaReservaDTO;
import com.javacadabra.tienda.inventario.aplicacion.puerto.salida.EventoProcesadoPuertoSalida;
import com.javacadabra.tienda.inventario.aplicacion.puerto.salida.StockRepositorioPuertoSalida;
import com.javacadabra.tienda.inventario.dominio.excepcion.StockInsuficienteException;
import com.javacadabra.tienda.inventario.dominio.modelo.agregado.Stock;
import com.javacadabra.tienda.inventario.dominio.modelo.objetovalor.Cantidad;
import com.javacadabra.tienda.inventario.dominio.modelo.objetovalor.ProductoId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservarStockServicioTest {

	@Mock
	private StockRepositorioPuertoSalida stockRepositorioPuertoSalida;

	@Mock
	private EventoProcesadoPuertoSalida eventoProcesadoPuertoSalida;

	@Test
	void reservarDecrementaElStockDeCadaLineaYMarcaElPedidoComoProcesado() {
		String productoId = UUID.randomUUID().toString();
		when(eventoProcesadoPuertoSalida.yaProcesado(any())).thenReturn(false);
		when(stockRepositorioPuertoSalida.buscarPorProductoId(any()))
				.thenReturn(Optional.of(Stock.crear(ProductoId.de(productoId), Cantidad.de(10))));

		var servicio = new ReservarStockServicio(stockRepositorioPuertoSalida, eventoProcesadoPuertoSalida);
		String pedidoId = UUID.randomUUID().toString();
		servicio.reservar(pedidoId, List.of(new LineaReservaDTO(productoId, 4)));

		verify(stockRepositorioPuertoSalida).guardar(any());
		verify(eventoProcesadoPuertoSalida).marcarProcesado(pedidoId);
	}

	@Test
	void reservarUnPedidoYaProcesadoNoTocaElStock() {
		when(eventoProcesadoPuertoSalida.yaProcesado(any())).thenReturn(true);

		var servicio = new ReservarStockServicio(stockRepositorioPuertoSalida, eventoProcesadoPuertoSalida);
		servicio.reservar(UUID.randomUUID().toString(), List.of(new LineaReservaDTO(UUID.randomUUID().toString(), 1)));

		verify(stockRepositorioPuertoSalida, never()).guardar(any());
		verify(eventoProcesadoPuertoSalida, never()).marcarProcesado(any());
	}

	@Test
	void reservarMasCantidadDeLaDisponibleLanzaExcepcion() {
		String productoId = UUID.randomUUID().toString();
		when(eventoProcesadoPuertoSalida.yaProcesado(any())).thenReturn(false);
		when(stockRepositorioPuertoSalida.buscarPorProductoId(any()))
				.thenReturn(Optional.of(Stock.crear(ProductoId.de(productoId), Cantidad.de(2))));

		var servicio = new ReservarStockServicio(stockRepositorioPuertoSalida, eventoProcesadoPuertoSalida);

		assertThatThrownBy(() -> servicio.reservar(UUID.randomUUID().toString(), List.of(new LineaReservaDTO(productoId, 5))))
				.isInstanceOf(StockInsuficienteException.class);
	}
}
