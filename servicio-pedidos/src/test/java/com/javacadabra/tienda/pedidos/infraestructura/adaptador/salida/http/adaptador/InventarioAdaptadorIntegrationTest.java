package com.javacadabra.tienda.pedidos.infraestructura.adaptador.salida.http.adaptador;

import com.javacadabra.tienda.pedidos.aplicacion.puerto.salida.InventarioPuertoSalida;
import com.javacadabra.tienda.pedidos.dominio.comando.ReservarStockComando;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.Cantidad;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.PedidoId;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.ProductoId;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.ReservaConfirmada;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.ReservaRechazada;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@SpringBootTest
@Testcontainers
class InventarioAdaptadorIntegrationTest {

	@Container
	@ServiceConnection
	static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest");

	@Autowired
	private InventarioPuertoSalida inventarioPuertoSalida;

	@Autowired
	private ConfiguracionMockInventario configuracionMockInventario;

	private MockRestServiceServer mockServer;

	@BeforeEach
	void reiniciar() {
		mockServer = configuracionMockInventario.mockServer();
		mockServer.reset();
	}

	@Test
	void reservarStockConExitoDevuelveReservaConfirmada() {
		mockServer.expect(requestTo("http://localhost:8082/api/v1/reservas-stock"))
				.andRespond(withStatus(HttpStatus.NO_CONTENT));

		var resultado = inventarioPuertoSalida.reservarStock(comandoDePrueba());

		assertThat(resultado).isInstanceOf(ReservaConfirmada.class);
		mockServer.verify();
	}

	@Test
	void reservarStockConStockInsuficienteDevuelveReservaRechazadaConElMotivo() {
		mockServer.expect(requestTo("http://localhost:8082/api/v1/reservas-stock"))
				.andRespond(withStatus(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_PROBLEM_JSON).body("""
						{"type":"about:blank","title":"Stock insuficiente","status":409,"detail":"Stock insuficiente para el producto X"}
						"""));

		var resultado = inventarioPuertoSalida.reservarStock(comandoDePrueba());

		assertThat(resultado).isInstanceOf(ReservaRechazada.class);
		assertThat(((ReservaRechazada) resultado).motivo()).isEqualTo("Stock insuficiente para el producto X");
		mockServer.verify();
	}

	@Test
	void reservarStockDeUnProductoSinStockDevuelveReservaRechazada() {
		mockServer.expect(requestTo("http://localhost:8082/api/v1/reservas-stock"))
				.andRespond(withStatus(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_PROBLEM_JSON).body("""
						{"type":"about:blank","title":"Stock no encontrado","status":404,"detail":"No existe stock registrado para el producto"}
						"""));

		var resultado = inventarioPuertoSalida.reservarStock(comandoDePrueba());

		assertThat(resultado).isInstanceOf(ReservaRechazada.class);
		mockServer.verify();
	}

	private static ReservarStockComando comandoDePrueba() {
		return new ReservarStockComando(PedidoId.generar(),
				List.of(new ReservarStockComando.LineaReserva(ProductoId.de(UUID.randomUUID().toString()), Cantidad.de(1))));
	}

	@TestConfiguration
	static class ConfiguracionMockInventario {

		private MockRestServiceServer mockServer;

		@Bean
		RestClientHttpServiceGroupConfigurer interceptarGrupoInventario() {
			return groups -> groups.filterByName("inventario")
					.forEachClient((group, builder) -> mockServer = MockRestServiceServer.createServer(builder));
		}

		MockRestServiceServer mockServer() {
			return mockServer;
		}
	}
}
