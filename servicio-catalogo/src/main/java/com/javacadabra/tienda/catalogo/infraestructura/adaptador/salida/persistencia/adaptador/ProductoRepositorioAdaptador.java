package com.javacadabra.tienda.catalogo.infraestructura.adaptador.salida.persistencia.adaptador;

import com.javacadabra.tienda.catalogo.aplicacion.puerto.salida.ProductoRepositorioPuertoSalida;
import com.javacadabra.tienda.catalogo.dominio.excepcion.CategoriaNoEncontradaException;
import com.javacadabra.tienda.catalogo.dominio.modelo.agregado.Producto;
import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.CategoriaId;
import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.ProductoId;
import com.javacadabra.tienda.catalogo.infraestructura.adaptador.salida.persistencia.mapper.ProductoEntidadMapper;
import com.javacadabra.tienda.catalogo.infraestructura.adaptador.salida.persistencia.repositorio.CategoriaRepositorioNeo4j;
import com.javacadabra.tienda.catalogo.infraestructura.adaptador.salida.persistencia.repositorio.ProductoRepositorioNeo4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductoRepositorioAdaptador implements ProductoRepositorioPuertoSalida {

	private final ProductoRepositorioNeo4j productoRepositorioNeo4j;
	private final CategoriaRepositorioNeo4j categoriaRepositorioNeo4j;
	private final ProductoEntidadMapper productoEntidadMapper;

	@Override
	public Producto guardar(Producto producto) {
		// Se resuelve la CategoriaEntidad real (no un stub con solo el id) para no
		// sobreescribir sus demás propiedades: Spring Data Neo4j guarda todo el grafo
		// alcanzable desde la raíz al hacer save().
		var categoriaEntidad = categoriaRepositorioNeo4j.findById(producto.categoriaId().valor())
				.orElseThrow(() -> new CategoriaNoEncontradaException(producto.categoriaId().valor()));
		var entidadGuardada = productoRepositorioNeo4j.save(productoEntidadMapper.aEntidad(producto, categoriaEntidad));
		return productoEntidadMapper.aDominio(entidadGuardada);
	}

	@Override
	public Optional<Producto> buscarPorId(ProductoId id) {
		return productoRepositorioNeo4j.findById(id.valor()).map(productoEntidadMapper::aDominio);
	}

	@Override
	public List<Producto> buscarPorCategoria(CategoriaId categoriaId) {
		return productoRepositorioNeo4j.buscarPorCategoriaId(categoriaId.valor()).stream()
				.map(productoEntidadMapper::aDominio)
				.toList();
	}

	@Override
	public List<Producto> buscarRecomendados(ProductoId productoId) {
		return productoRepositorioNeo4j.buscarRecomendados(productoId.valor()).stream()
				.map(productoEntidadMapper::aDominio)
				.toList();
	}

	@Override
	public void agregarRecomendacion(ProductoId productoId, ProductoId recomendadoId) {
		productoRepositorioNeo4j.agregarRecomendacion(productoId.valor(), recomendadoId.valor());
	}
}
