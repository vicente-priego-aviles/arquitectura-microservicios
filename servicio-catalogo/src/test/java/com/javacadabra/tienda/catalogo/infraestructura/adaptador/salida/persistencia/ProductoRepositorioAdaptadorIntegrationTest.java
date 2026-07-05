package com.javacadabra.tienda.catalogo.infraestructura.adaptador.salida.persistencia;

import com.javacadabra.tienda.catalogo.dominio.modelo.agregado.Producto;
import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.Precio;
import com.javacadabra.tienda.catalogo.infraestructura.adaptador.salida.persistencia.adaptador.ProductoRepositorioAdaptador;
import com.javacadabra.tienda.catalogo.infraestructura.adaptador.salida.persistencia.mapper.ProductoEntidadMapperImpl;
import com.javacadabra.tienda.catalogo.infraestructura.adaptador.salida.persistencia.repositorio.ProductoRepositorioNeo4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.neo4j.test.autoconfigure.DataNeo4jTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataNeo4jTest
@Testcontainers
@Import({ProductoRepositorioAdaptador.class, ProductoEntidadMapperImpl.class})
class ProductoRepositorioAdaptadorIntegrationTest {

	@Container
	@ServiceConnection
	static final Neo4jContainer<?> neo4jContainer = new Neo4jContainer<>("neo4j:latest");

	@Autowired
	private ProductoRepositorioAdaptador productoRepositorioAdaptador;

	@Autowired
	private ProductoRepositorioNeo4j productoRepositorioNeo4j;

	@Test
	void guardaYRecuperaUnProductoPorId() {
		Producto producto = Producto.crear("Camiseta", "100% algodón", Precio.de(new BigDecimal("19.99")));

		productoRepositorioAdaptador.guardar(producto);
		Optional<Producto> recuperado = productoRepositorioAdaptador.buscarPorId(producto.id());

		assertThat(recuperado).isPresent();
		assertThat(recuperado.get().nombre()).isEqualTo("Camiseta");
		assertThat(recuperado.get().precio().valor()).isEqualByComparingTo("19.99");
	}

	@Test
	void buscarUnIdInexistenteDevuelveVacio() {
		var idInexistente = com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.ProductoId.generar();

		assertThat(productoRepositorioAdaptador.buscarPorId(idInexistente)).isEmpty();
	}
}
