package com.javacadabra.tienda.pedidos.infraestructura.adaptador.entrada.rest;

import com.javacadabra.tienda.pedidos.aplicacion.dto.entrada.CrearPedidoDTO;
import com.javacadabra.tienda.pedidos.aplicacion.dto.entrada.LineaPedidoDTO;
import com.javacadabra.tienda.pedidos.aplicacion.dto.salida.PedidoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "outbox.poller.enabled=false")
@AutoConfigureRestTestClient
@Testcontainers
class PedidoControllerRestTestClientIntegrationTest {

	@Container
	@ServiceConnection
	static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest");

	@Autowired
	private RestTestClient restTestClient;

	@Autowired
	private ConfiguracionMockCatalogo configuracionMockCatalogo;

	private MockRestServiceServer mockServer;

	@BeforeEach
	void reiniciar() {
		mockServer = configuracionMockCatalogo.mockServer();
		mockServer.reset();
	}

	@Test
	void crearUnPedidoDeExtremoAExtremoConServidorHttpRealYCabecerasVerificadas() {
		String productoId = UUID.randomUUID().toString();
		mockServer.expect(requestTo("http://localhost:8080/api/productos/" + productoId))
				.andRespond(withSuccess("""
						{"id":"%s","nombre":"Teclado","descripcion":"Switches azules","precio":49.99,"categoriaId":"%s"}
						""".formatted(productoId, UUID.randomUUID()), MediaType.APPLICATION_JSON));

		CrearPedidoDTO dto = new CrearPedidoDTO(UUID.randomUUID().toString(), List.of(new LineaPedidoDTO(productoId, 2)));

		restTestClient.post().uri("/api/pedidos")
				.contentType(MediaType.APPLICATION_JSON)
				.body(dto)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody(PedidoDTO.class)
				.value(pedido -> assertThat(pedido.total()).isEqualByComparingTo("99.98"));

		mockServer.verify();
	}

	@TestConfiguration
	static class ConfiguracionMockCatalogo {

		private MockRestServiceServer mockServer;

		@Bean
		RestClientHttpServiceGroupConfigurer interceptarGrupoCatalogo() {
			return groups -> groups.filterByName("catalogo")
					.forEachClient((group, builder) -> mockServer = MockRestServiceServer.createServer(builder));
		}

		MockRestServiceServer mockServer() {
			return mockServer;
		}
	}
}
