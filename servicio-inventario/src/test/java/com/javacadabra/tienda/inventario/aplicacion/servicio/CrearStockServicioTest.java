package com.javacadabra.tienda.inventario.aplicacion.servicio;

import com.javacadabra.tienda.inventario.aplicacion.puerto.salida.StockRepositorioPuertoSalida;
import com.javacadabra.tienda.inventario.dominio.modelo.agregado.Stock;
import com.javacadabra.tienda.inventario.dominio.modelo.objetovalor.Cantidad;
import com.javacadabra.tienda.inventario.dominio.modelo.objetovalor.ProductoId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrearStockServicioTest {

	@Mock
	private StockRepositorioPuertoSalida stockRepositorioPuertoSalida;

	@Test
	void crearStockNuevoLoGuardaConLaCantidadInicial() {
		String productoId = UUID.randomUUID().toString();
		when(stockRepositorioPuertoSalida.buscarPorProductoId(any())).thenReturn(Optional.empty());
		when(stockRepositorioPuertoSalida.guardar(any())).thenAnswer(invocacion -> invocacion.getArgument(0));

		new CrearStockServicio(stockRepositorioPuertoSalida).crear(productoId);

		ArgumentCaptor<Stock> captor = ArgumentCaptor.forClass(Stock.class);
		verify(stockRepositorioPuertoSalida).guardar(captor.capture());
		assertThat(captor.getValue().productoId()).isEqualTo(ProductoId.de(productoId));
		assertThat(captor.getValue().cantidad()).isEqualTo(Cantidad.de(CrearStockServicio.CANTIDAD_INICIAL));
	}

	@Test
	void crearStockYaExistenteNoLoSobreescribe() {
		String productoId = UUID.randomUUID().toString();
		when(stockRepositorioPuertoSalida.buscarPorProductoId(any()))
				.thenReturn(Optional.of(Stock.crear(ProductoId.de(productoId), Cantidad.de(3))));

		new CrearStockServicio(stockRepositorioPuertoSalida).crear(productoId);

		verify(stockRepositorioPuertoSalida, never()).guardar(any());
	}
}
