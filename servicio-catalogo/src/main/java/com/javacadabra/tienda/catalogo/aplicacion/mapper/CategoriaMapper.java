package com.javacadabra.tienda.catalogo.aplicacion.mapper;

import com.javacadabra.tienda.catalogo.aplicacion.dto.salida.CategoriaDTO;
import com.javacadabra.tienda.catalogo.dominio.modelo.agregado.Categoria;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoriaMapper {

	default CategoriaDTO aDTO(Categoria categoria) {
		if (categoria == null) {
			return null;
		}
		return new CategoriaDTO(categoria.id().valor(), categoria.nombre());
	}
}
