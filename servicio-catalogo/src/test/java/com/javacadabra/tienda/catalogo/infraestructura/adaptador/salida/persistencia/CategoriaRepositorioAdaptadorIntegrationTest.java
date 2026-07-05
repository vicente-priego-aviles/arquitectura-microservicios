package com.javacadabra.tienda.catalogo.infraestructura.adaptador.salida.persistencia;

import com.javacadabra.tienda.catalogo.dominio.modelo.agregado.Categoria;
import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.CategoriaId;
import com.javacadabra.tienda.catalogo.infraestructura.adaptador.salida.persistencia.adaptador.CategoriaRepositorioAdaptador;
import com.javacadabra.tienda.catalogo.infraestructura.adaptador.salida.persistencia.mapper.CategoriaEntidadMapperImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.neo4j.test.autoconfigure.DataNeo4jTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataNeo4jTest
@Testcontainers
@Import({CategoriaRepositorioAdaptador.class, CategoriaEntidadMapperImpl.class})
class CategoriaRepositorioAdaptadorIntegrationTest {

	@Container
	@ServiceConnection
	static final Neo4jContainer<?> neo4jContainer = new Neo4jContainer<>("neo4j:latest");

	@Autowired
	private CategoriaRepositorioAdaptador categoriaRepositorioAdaptador;

	@Test
	void guardaYRecuperaUnaCategoriaPorId() {
		Categoria categoria = Categoria.crear("Ropa");

		categoriaRepositorioAdaptador.guardar(categoria);
		Optional<Categoria> recuperada = categoriaRepositorioAdaptador.buscarPorId(categoria.id());

		assertThat(recuperada).isPresent();
		assertThat(recuperada.get().nombre()).isEqualTo("Ropa");
	}

	@Test
	void buscarUnIdInexistenteDevuelveVacio() {
		assertThat(categoriaRepositorioAdaptador.buscarPorId(CategoriaId.generar())).isEmpty();
	}
}
