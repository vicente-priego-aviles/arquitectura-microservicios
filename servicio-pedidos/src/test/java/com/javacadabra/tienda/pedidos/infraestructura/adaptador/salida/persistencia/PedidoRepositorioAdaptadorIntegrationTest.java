package com.javacadabra.tienda.pedidos.infraestructura.adaptador.salida.persistencia;

import com.javacadabra.tienda.pedidos.dominio.modelo.agregado.Pedido;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.Cantidad;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.ClienteId;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.Precio;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.ProductoId;
import com.javacadabra.tienda.pedidos.infraestructura.adaptador.salida.persistencia.adaptador.PedidoRepositorioAdaptador;
import com.javacadabra.tienda.pedidos.infraestructura.adaptador.salida.persistencia.entidad.PedidoEntidad;
import com.javacadabra.tienda.pedidos.infraestructura.adaptador.salida.persistencia.mapper.PedidoEntidadMapperImpl;
import com.javacadabra.tienda.pedidos.infraestructura.adaptador.salida.persistencia.repositorio.PedidoRepositorioJpa;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Testcontainers
@Import({PedidoRepositorioAdaptador.class, PedidoEntidadMapperImpl.class})
class PedidoRepositorioAdaptadorIntegrationTest {

	@Container
	@ServiceConnection
	static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest");

	@Autowired
	private PedidoRepositorioAdaptador pedidoRepositorioAdaptador;

	@Autowired
	private PedidoRepositorioJpa pedidoRepositorioJpa;

	@Test
	void guardaUnPedidoConSusLineas() {
		Pedido pedido = Pedido.crear(ClienteId.de(UUID.randomUUID().toString()));
		pedido.agregarLinea(ProductoId.de(UUID.randomUUID().toString()), Cantidad.de(2), Precio.de(new BigDecimal("19.99")));

		Pedido guardado = pedidoRepositorioAdaptador.guardar(pedido);

		Optional<PedidoEntidad> entidad = pedidoRepositorioJpa.findById(guardado.id().valor());
		assertThat(entidad).isPresent();
		assertThat(entidad.get().getClienteId()).isEqualTo(pedido.clienteId().valor());
		assertThat(entidad.get().getLineas()).hasSize(1);
		assertThat(entidad.get().getLineas().getFirst().getPrecioUnitario()).isEqualByComparingTo("19.99");
	}

	@Test
	void guardaUnPedidoSinLineas() {
		Pedido pedido = Pedido.crear(ClienteId.de(UUID.randomUUID().toString()));

		Pedido guardado = pedidoRepositorioAdaptador.guardar(pedido);

		assertThat(pedidoRepositorioJpa.findById(guardado.id().valor())).isPresent();
		assertThat(guardado.lineas()).isEmpty();
	}
}
