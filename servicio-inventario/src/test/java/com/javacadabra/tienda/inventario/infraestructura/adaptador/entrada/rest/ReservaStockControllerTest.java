package com.javacadabra.tienda.inventario.infraestructura.adaptador.entrada.rest;

import com.javacadabra.tienda.inventario.aplicacion.puerto.entrada.ReservarStockPuertoEntrada;
import com.javacadabra.tienda.inventario.dominio.excepcion.StockInsuficienteException;
import com.javacadabra.tienda.inventario.dominio.excepcion.StockNoEncontradoException;
import com.javacadabra.tienda.inventario.dominio.modelo.objetovalor.ProductoId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@WebMvcTest(ReservaStockController.class)
class ReservaStockControllerTest {

	@Autowired
	private MockMvcTester mvc;

	@MockitoBean
	private ReservarStockPuertoEntrada reservarStockPuertoEntrada;

	@Test
	void reservarConExitoDevuelve204YLlamaAlPuertoDeEntrada() {
		String pedidoId = UUID.randomUUID().toString();
		String productoId = UUID.randomUUID().toString();

		assertThat(mvc.post().uri("/api/v1/reservas-stock")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"pedidoId":"%s","lineas":[{"productoId":"%s","cantidad":2}]}
						""".formatted(pedidoId, productoId)))
				.hasStatus(HttpStatus.NO_CONTENT);

		verify(reservarStockPuertoEntrada).reservar(eq(pedidoId), any());
	}

	@Test
	void reservarConStockInsuficienteDevuelve409ConProblemDetail() {
		String pedidoId = UUID.randomUUID().toString();
		String productoId = UUID.randomUUID().toString();
		doThrow(new StockInsuficienteException(ProductoId.de(productoId), 2, 5))
				.when(reservarStockPuertoEntrada).reservar(any(), any());

		assertThat(mvc.post().uri("/api/v1/reservas-stock")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"pedidoId":"%s","lineas":[{"productoId":"%s","cantidad":5}]}
						""".formatted(pedidoId, productoId)))
				.hasStatus(HttpStatus.CONFLICT)
				.bodyJson()
				.extractingPath("$.title").isEqualTo("Stock insuficiente");
	}

	@Test
	void reservarUnProductoSinStockDevuelve404ConProblemDetail() {
		String pedidoId = UUID.randomUUID().toString();
		String productoId = UUID.randomUUID().toString();
		doThrow(new StockNoEncontradoException(ProductoId.de(productoId)))
				.when(reservarStockPuertoEntrada).reservar(any(), any());

		assertThat(mvc.post().uri("/api/v1/reservas-stock")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"pedidoId":"%s","lineas":[{"productoId":"%s","cantidad":1}]}
						""".formatted(pedidoId, productoId)))
				.hasStatus(HttpStatus.NOT_FOUND)
				.bodyJson()
				.extractingPath("$.title").isEqualTo("Stock no encontrado");
	}
}
