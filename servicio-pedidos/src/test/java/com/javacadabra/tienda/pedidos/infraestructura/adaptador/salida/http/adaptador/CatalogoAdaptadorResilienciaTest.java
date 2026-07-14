package com.javacadabra.tienda.pedidos.infraestructura.adaptador.salida.http.adaptador;

import com.javacadabra.tienda.pedidos.aplicacion.puerto.salida.CatalogoPuertoSalida;
import com.javacadabra.tienda.pedidos.dominio.excepcion.CatalogoNoDisponibleException;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.ProductoId;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest(properties = "outbox.poller.enabled=false")
@Testcontainers
class CatalogoAdaptadorResilienciaTest {

	@Container
	@ServiceConnection
	static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest");

	@Autowired
	private CatalogoPuertoSalida catalogoPuertoSalida;

	@Autowired
	private ConfiguracionMockCatalogo configuracionMockCatalogo;

	@Autowired
	private CircuitBreakerRegistry circuitBreakerRegistry;

	private MockRestServiceServer mockServer;

	@BeforeEach
	void reiniciar() {
		mockServer = configuracionMockCatalogo.mockServer();
		mockServer.reset();
		circuitBreakerRegistry.circuitBreaker("catalogo").reset();
	}

	@Test
	void reintentaTrasFallosDeRedYFinalmentePropagaElFalloSiElCatalogoNoResponde() {
		ProductoId productoId = ProductoId.de(UUID.randomUUID().toString());
		mockServer.expect(ExpectedCount.times(4), requestTo("http://localhost:8080/api/productos/" + productoId.valor()))
				.andRespond(request -> {
					throw new IOException("Fallo de red simulado");
				});

		assertThatThrownBy(() -> catalogoPuertoSalida.buscarProductoPorId(productoId))
				.isInstanceOf(ResourceAccessException.class);

		mockServer.verify();
	}

	@Test
	void trasAbrirseElCircuitoLasSiguientesLlamadasFallanRapidoSinLlamarAlCatalogo() {
		ProductoId productoId = ProductoId.de(UUID.randomUUID().toString());
		mockServer.expect(ExpectedCount.times(4), requestTo("http://localhost:8080/api/productos/" + productoId.valor()))
				.andRespond(request -> {
					throw new IOException("Fallo de red simulado");
				});

		assertThatThrownBy(() -> catalogoPuertoSalida.buscarProductoPorId(productoId))
				.isInstanceOf(ResourceAccessException.class);
		assertThat(circuitBreakerRegistry.circuitBreaker("catalogo").getState())
				.isEqualTo(CircuitBreaker.State.OPEN);

		ProductoId segundaLlamada = ProductoId.de(UUID.randomUUID().toString());
		assertThatThrownBy(() -> catalogoPuertoSalida.buscarProductoPorId(segundaLlamada))
				.isInstanceOf(CatalogoNoDisponibleException.class);

		mockServer.verify();
	}

	@Test
	void recuperaCuandoElCatalogoVuelveAResponder() {
		ProductoId productoId = ProductoId.de(UUID.randomUUID().toString());
		mockServer.expect(requestTo("http://localhost:8080/api/productos/" + productoId.valor()))
				.andRespond(withSuccess("""
						{"id":"%s","nombre":"Teclado","descripcion":"Switches azules","precio":49.99,"categoriaId":"%s"}
						""".formatted(productoId.valor(), UUID.randomUUID()), MediaType.APPLICATION_JSON));

		var producto = catalogoPuertoSalida.buscarProductoPorId(productoId);

		assertThat(producto.nombre()).isEqualTo("Teclado");
		assertThat(producto.precio()).isEqualByComparingTo("49.99");
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
