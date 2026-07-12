package com.javacadabra.tienda.catalogo.infraestructura.adaptador.salida.persistencia.adaptador;

import com.javacadabra.tienda.catalogo.aplicacion.puerto.salida.CategoriaRepositorioPuertoSalida;
import com.javacadabra.tienda.catalogo.dominio.modelo.agregado.Categoria;
import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.CategoriaId;
import com.javacadabra.tienda.catalogo.infraestructura.adaptador.salida.persistencia.mapper.CategoriaEntidadMapper;
import com.javacadabra.tienda.catalogo.infraestructura.adaptador.salida.persistencia.repositorio.CategoriaRepositorioNeo4j;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.stereotype.Component;

import java.util.Optional;

@SecondaryAdapter
@Component
@RequiredArgsConstructor
public class CategoriaRepositorioAdaptador implements CategoriaRepositorioPuertoSalida {

	private final CategoriaRepositorioNeo4j categoriaRepositorioNeo4j;
	private final CategoriaEntidadMapper categoriaEntidadMapper;

	@Override
	public Categoria guardar(Categoria categoria) {
		var entidadGuardada = categoriaRepositorioNeo4j.save(categoriaEntidadMapper.aEntidad(categoria));
		return categoriaEntidadMapper.aDominio(entidadGuardada);
	}

	@Override
	public Optional<Categoria> buscarPorId(CategoriaId id) {
		return categoriaRepositorioNeo4j.findById(id.valor()).map(categoriaEntidadMapper::aDominio);
	}
}
