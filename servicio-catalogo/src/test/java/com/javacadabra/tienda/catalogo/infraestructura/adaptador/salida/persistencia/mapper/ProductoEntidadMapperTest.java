package com.javacadabra.tienda.catalogo.infraestructura.adaptador.salida.persistencia.mapper;

import com.javacadabra.tienda.catalogo.dominio.modelo.agregado.Producto;
import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.CategoriaId;
import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.Precio;
import com.javacadabra.tienda.catalogo.infraestructura.adaptador.salida.persistencia.entidad.CategoriaEntidad;
import com.javacadabra.tienda.catalogo.infraestructura.adaptador.salida.persistencia.entidad.ProductoEntidad;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

class ProductoEntidadMapperTest {

	private final ProductoEntidadMapper mapper = new ProductoEntidadMapperImpl();
	private final Faker faker = new Faker();

	@Test
	void mapeaUnaEntidadDePersistenciaAlAgregadoDeDominio() {
		// La entidad no tiene invariantes propias, pero sus valores acaban pasando
		// por los constructores validados de ProductoId/Precio en aDominio(): sin
		// estos generadores dirigidos, Instancio podría producir un id que no es
		// un UUID o un precio negativo, y el mapeo lanzaría IllegalArgumentException.
		ProductoEntidad entidad = Instancio.of(ProductoEntidad.class)
				.generate(field(ProductoEntidad::getId), gen -> gen.text().uuid())
				.generate(field(ProductoEntidad::getPrecio), gen -> gen.math().bigDecimal().min(BigDecimal.ZERO))
				.generate(field(CategoriaEntidad::getId), gen -> gen.text().uuid())
				.create();

		Producto producto = mapper.aDominio(entidad);

		assertThat(producto.id().valor()).isEqualTo(entidad.getId());
		assertThat(producto.nombre()).isEqualTo(entidad.getNombre());
		assertThat(producto.descripcion()).isEqualTo(entidad.getDescripcion());
		assertThat(producto.precio().valor()).isEqualByComparingTo(entidad.getPrecio());
		assertThat(producto.categoriaId().valor()).isEqualTo(entidad.getCategoria().getId());
		assertThat(producto.fechaCreacion()).isEqualTo(entidad.getFechaCreacion());
	}

	@Test
	void mapeaUnProductoDeDominioAUnaEntidadDePersistencia() {
		// Aquí el sentido va al revés: producto ya es válido (nace de Producto.crear,
		// con DataFaker aportando valores realistas), así que la categoriaEntidad de
		// destino no necesita ningún generador dirigido — aEntidad() no la valida,
		// solo la enlaza tal cual.
		Producto producto = Producto.crear(
				faker.commerce().productName(),
				faker.lorem().sentence(),
				Precio.de(new BigDecimal(faker.commerce().price(1, 500))),
				CategoriaId.generar());
		CategoriaEntidad categoriaEntidad = Instancio.create(CategoriaEntidad.class);

		ProductoEntidad entidad = mapper.aEntidad(producto, categoriaEntidad);

		assertThat(entidad.getId()).isEqualTo(producto.id().valor());
		assertThat(entidad.getNombre()).isEqualTo(producto.nombre());
		assertThat(entidad.getDescripcion()).isEqualTo(producto.descripcion());
		assertThat(entidad.getPrecio()).isEqualByComparingTo(producto.precio().valor());
		assertThat(entidad.getCategoria()).isEqualTo(categoriaEntidad);
	}
}
