package com.javacadabra.tienda.catalogo.infraestructura.adaptador.salida.persistencia.mapper;

import com.javacadabra.tienda.catalogo.dominio.modelo.agregado.Producto;
import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.Precio;
import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.ProductoId;
import com.javacadabra.tienda.catalogo.infraestructura.adaptador.salida.persistencia.entidad.ProductoEntidad;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductoEntidadMapper {

	default ProductoEntidad aEntidad(Producto producto) {
		if (producto == null) {
			return null;
		}
		return new ProductoEntidad(
				producto.id().valor(),
				producto.nombre(),
				producto.descripcion(),
				producto.precio().valor(),
				producto.fechaCreacion());
	}

	default Producto aDominio(ProductoEntidad entidad) {
		if (entidad == null) {
			return null;
		}
		return Producto.reconstruir(
				ProductoId.de(entidad.getId()),
				entidad.getNombre(),
				entidad.getDescripcion(),
				Precio.de(entidad.getPrecio()),
				entidad.getFechaCreacion());
	}
}
