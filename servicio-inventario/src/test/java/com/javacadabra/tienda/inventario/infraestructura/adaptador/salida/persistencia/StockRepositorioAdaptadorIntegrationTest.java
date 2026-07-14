package com.javacadabra.tienda.inventario.infraestructura.adaptador.salida.persistencia;

import com.javacadabra.tienda.inventario.dominio.modelo.agregado.Stock;
import com.javacadabra.tienda.inventario.dominio.modelo.objetovalor.Cantidad;
import com.javacadabra.tienda.inventario.dominio.modelo.objetovalor.ProductoId;
import com.javacadabra.tienda.inventario.infraestructura.adaptador.salida.persistencia.adaptador.StockRepositorioAdaptador;
import com.javacadabra.tienda.inventario.infraestructura.adaptador.salida.persistencia.mapper.StockEntidadMapperImpl;
import com.javacadabra.tienda.inventario.infraestructura.adaptador.salida.persistencia.repositorio.StockRepositorioJpa;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Testcontainers
@Import({StockRepositorioAdaptador.class, StockEntidadMapperImpl.class})
class StockRepositorioAdaptadorIntegrationTest {

	@Container
	@ServiceConnection
	static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest");

	@Autowired
	private StockRepositorioAdaptador stockRepositorioAdaptador;

	@Autowired
	private StockRepositorioJpa stockRepositorioJpa;

	@Test
	void guardaYRecuperaElStockDeUnProducto() {
		ProductoId productoId = ProductoId.de(UUID.randomUUID().toString());
		Stock stock = Stock.crear(productoId, Cantidad.de(100));

		stockRepositorioAdaptador.guardar(stock);

		assertThat(stockRepositorioJpa.findById(productoId.valor())).isPresent();
		assertThat(stockRepositorioAdaptador.buscarPorProductoId(productoId))
				.hasValueSatisfying(encontrado -> assertThat(encontrado.cantidad()).isEqualTo(Cantidad.de(100)));
	}

	@Test
	void buscarUnProductoSinStockDevuelveVacio() {
		assertThat(stockRepositorioAdaptador.buscarPorProductoId(ProductoId.de(UUID.randomUUID().toString())))
				.isEmpty();
	}
}
