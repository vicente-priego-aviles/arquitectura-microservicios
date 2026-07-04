package com.javacadabra.tienda.catalogo.infraestructura.adaptador.salida.persistencia.adaptador;

import com.javacadabra.tienda.catalogo.aplicacion.puerto.salida.ProductoRepositorioPuertoSalida;
import com.javacadabra.tienda.catalogo.dominio.modelo.agregado.Producto;
import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.ProductoId;
import com.javacadabra.tienda.catalogo.infraestructura.adaptador.salida.persistencia.mapper.ProductoEntidadMapper;
import com.javacadabra.tienda.catalogo.infraestructura.adaptador.salida.persistencia.repositorio.ProductoRepositorioNeo4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductoRepositorioAdaptador implements ProductoRepositorioPuertoSalida {

	private final ProductoRepositorioNeo4j productoRepositorioNeo4j;
	private final ProductoEntidadMapper productoEntidadMapper;

	@Override
	public Producto guardar(Producto producto) {
		var entidadGuardada = productoRepositorioNeo4j.save(productoEntidadMapper.aEntidad(producto));
		return productoEntidadMapper.aDominio(entidadGuardada);
	}

	@Override
	public Optional<Producto> buscarPorId(ProductoId id) {
		return productoRepositorioNeo4j.findById(id.valor()).map(productoEntidadMapper::aDominio);
	}
}
