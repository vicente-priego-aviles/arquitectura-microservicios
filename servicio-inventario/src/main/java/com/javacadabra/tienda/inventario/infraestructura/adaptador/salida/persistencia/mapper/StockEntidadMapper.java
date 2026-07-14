package com.javacadabra.tienda.inventario.infraestructura.adaptador.salida.persistencia.mapper;

import com.javacadabra.tienda.inventario.dominio.modelo.agregado.Stock;
import com.javacadabra.tienda.inventario.dominio.modelo.objetovalor.Cantidad;
import com.javacadabra.tienda.inventario.dominio.modelo.objetovalor.ProductoId;
import com.javacadabra.tienda.inventario.infraestructura.adaptador.salida.persistencia.entidad.StockEntidad;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StockEntidadMapper {

	default StockEntidad aEntidad(Stock stock) {
		if (stock == null) {
			return null;
		}
		return new StockEntidad(stock.productoId().valor(), stock.cantidad().valor());
	}

	default Stock aDominio(StockEntidad entidad) {
		if (entidad == null) {
			return null;
		}
		return Stock.reconstruir(ProductoId.de(entidad.getProductoId()), Cantidad.de(entidad.getCantidad()));
	}
}
