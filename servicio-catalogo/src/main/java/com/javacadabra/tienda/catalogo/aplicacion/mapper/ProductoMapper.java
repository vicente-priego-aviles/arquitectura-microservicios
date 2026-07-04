package com.javacadabra.tienda.catalogo.aplicacion.mapper;

import com.javacadabra.tienda.catalogo.aplicacion.dto.salida.ProductoDTO;
import com.javacadabra.tienda.catalogo.dominio.modelo.agregado.Producto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductoMapper {

	default ProductoDTO aDTO(Producto producto) {
		if (producto == null) {
			return null;
		}
		return new ProductoDTO(
				producto.id().valor(),
				producto.nombre(),
				producto.descripcion(),
				producto.precio().valor());
	}
}
