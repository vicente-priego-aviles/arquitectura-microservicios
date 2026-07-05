package com.javacadabra.tienda.catalogo.infraestructura.adaptador.salida.persistencia;

import com.javacadabra.tienda.catalogo.dominio.modelo.agregado.Categoria;
import com.javacadabra.tienda.catalogo.dominio.modelo.agregado.Producto;
import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.CategoriaId;
import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.Precio;
import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.ProductoId;
import com.javacadabra.tienda.catalogo.infraestructura.adaptador.salida.persistencia.adaptador.CategoriaRepositorioAdaptador;
import com.javacadabra.tienda.catalogo.infraestructura.adaptador.salida.persistencia.adaptador.ProductoRepositorioAdaptador;
import com.javacadabra.tienda.catalogo.infraestructura.adaptador.salida.persistencia.mapper.CategoriaEntidadMapperImpl;
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
@Import({ProductoRepositorioAdaptador.class, ProductoEntidadMapperImpl.class, CategoriaRepositorioAdaptador.class, CategoriaEntidadMapperImpl.class})
class ProductoRepositorioAdaptadorIntegrationTest {

	@Container
	@ServiceConnection
	static final Neo4jContainer<?> neo4jContainer = new Neo4jContainer<>("neo4j:latest");

	@Autowired
	private ProductoRepositorioAdaptador productoRepositorioAdaptador;

	@Autowired
	private CategoriaRepositorioAdaptador categoriaRepositorioAdaptador;

	@Autowired
	private ProductoRepositorioNeo4j productoRepositorioNeo4j;

	@Test
	void guardaYRecuperaUnProductoPorId() {
		CategoriaId categoriaId = categoriaRepositorioAdaptador.guardar(Categoria.crear("Ropa")).id();
		Producto producto = Producto.crear("Camiseta", "100% algodón", Precio.de(new BigDecimal("19.99")), categoriaId);

		productoRepositorioAdaptador.guardar(producto);
		Optional<Producto> recuperado = productoRepositorioAdaptador.buscarPorId(producto.id());

		assertThat(recuperado).isPresent();
		assertThat(recuperado.get().nombre()).isEqualTo("Camiseta");
		assertThat(recuperado.get().precio().valor()).isEqualByComparingTo("19.99");
		assertThat(recuperado.get().categoriaId()).isEqualTo(categoriaId);
	}

	@Test
	void buscarUnIdInexistenteDevuelveVacio() {
		assertThat(productoRepositorioAdaptador.buscarPorId(ProductoId.generar())).isEmpty();
	}

	@Test
	void guardarDosProductosEnLaMismaCategoriaNoSobrescribeElNombreDeLaCategoria() {
		CategoriaId categoriaId = categoriaRepositorioAdaptador.guardar(Categoria.crear("Ropa")).id();

		productoRepositorioAdaptador.guardar(Producto.crear("Camiseta", "100% algodón", Precio.de(BigDecimal.TEN), categoriaId));
		productoRepositorioAdaptador.guardar(Producto.crear("Pantalón", "Vaquero", Precio.de(BigDecimal.TEN), categoriaId));

		Optional<Categoria> categoria = categoriaRepositorioAdaptador.buscarPorId(categoriaId);

		assertThat(categoria).isPresent();
		assertThat(categoria.get().nombre()).isEqualTo("Ropa");
	}

	@Test
	void buscaLosProductosDeUnaCategoria() {
		CategoriaId categoriaId = categoriaRepositorioAdaptador.guardar(Categoria.crear("Ropa")).id();
		CategoriaId otraCategoriaId = categoriaRepositorioAdaptador.guardar(Categoria.crear("Electrónica")).id();

		Producto camiseta = productoRepositorioAdaptador.guardar(Producto.crear("Camiseta", "100% algodón", Precio.de(BigDecimal.TEN), categoriaId));
		productoRepositorioAdaptador.guardar(Producto.crear("Teléfono", "Smartphone", Precio.de(BigDecimal.TEN), otraCategoriaId));

		var productosDeRopa = productoRepositorioAdaptador.buscarPorCategoria(categoriaId);

		assertThat(productosDeRopa).extracting(Producto::id).containsExactly(camiseta.id());
	}

	@Test
	void agregaYRecuperaUnaRecomendacionEntreProductos() {
		CategoriaId categoriaId = categoriaRepositorioAdaptador.guardar(Categoria.crear("Ropa")).id();
		Producto camiseta = productoRepositorioAdaptador.guardar(Producto.crear("Camiseta", "100% algodón", Precio.de(BigDecimal.TEN), categoriaId));
		Producto pantalon = productoRepositorioAdaptador.guardar(Producto.crear("Pantalón", "Vaquero", Precio.de(BigDecimal.TEN), categoriaId));

		productoRepositorioAdaptador.agregarRecomendacion(camiseta.id(), pantalon.id());
		var recomendados = productoRepositorioAdaptador.buscarRecomendados(camiseta.id());

		assertThat(recomendados).extracting(Producto::id).containsExactly(pantalon.id());
	}
}
