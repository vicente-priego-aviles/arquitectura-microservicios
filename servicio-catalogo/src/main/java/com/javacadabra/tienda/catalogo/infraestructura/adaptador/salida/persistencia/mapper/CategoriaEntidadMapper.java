package com.javacadabra.tienda.catalogo.infraestructura.adaptador.salida.persistencia.mapper;

import com.javacadabra.tienda.catalogo.dominio.modelo.agregado.Categoria;
import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.CategoriaId;
import com.javacadabra.tienda.catalogo.infraestructura.adaptador.salida.persistencia.entidad.CategoriaEntidad;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoriaEntidadMapper {

	default CategoriaEntidad aEntidad(Categoria categoria) {
		if (categoria == null) {
			return null;
		}
		return new CategoriaEntidad(categoria.id().valor(), categoria.nombre());
	}

	default Categoria aDominio(CategoriaEntidad entidad) {
		if (entidad == null) {
			return null;
		}
		return Categoria.reconstruir(CategoriaId.de(entidad.getId()), entidad.getNombre());
	}
}
