package com.javacadabra.tienda.pedidos.infraestructura.adaptador.salida.persistencia.mapper;

import com.javacadabra.tienda.pedidos.dominio.modelo.agregado.Pedido;
import com.javacadabra.tienda.pedidos.dominio.modelo.entidad.LineaPedido;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.Cantidad;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.ClienteId;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.PedidoId;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.Precio;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.ProductoId;
import com.javacadabra.tienda.pedidos.infraestructura.adaptador.salida.persistencia.entidad.LineaPedidoEntidad;
import com.javacadabra.tienda.pedidos.infraestructura.adaptador.salida.persistencia.entidad.PedidoEntidad;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PedidoEntidadMapper {

	default PedidoEntidad aEntidad(Pedido pedido) {
		if (pedido == null) {
			return null;
		}
		PedidoEntidad entidad = new PedidoEntidad(pedido.id().valor(), pedido.clienteId().valor(), pedido.fechaCreacion(), new ArrayList<>());
		List<LineaPedidoEntidad> lineas = pedido.lineas().stream()
				.map(linea -> new LineaPedidoEntidad(null, linea.productoId().valor(), linea.cantidad().valor(), linea.precioUnitario().valor(), entidad))
				.toList();
		entidad.getLineas().addAll(lineas);
		return entidad;
	}

	default Pedido aDominio(PedidoEntidad entidad) {
		if (entidad == null) {
			return null;
		}
		List<LineaPedido> lineas = entidad.getLineas().stream().map(this::aDominio).toList();
		return Pedido.reconstruir(PedidoId.de(entidad.getId()), ClienteId.de(entidad.getClienteId()), lineas, entidad.getFechaCreacion());
	}

	default LineaPedido aDominio(LineaPedidoEntidad entidad) {
		if (entidad == null) {
			return null;
		}
		return LineaPedido.crear(ProductoId.de(entidad.getProductoId()), Cantidad.de(entidad.getCantidad()), Precio.de(entidad.getPrecioUnitario()));
	}
}
