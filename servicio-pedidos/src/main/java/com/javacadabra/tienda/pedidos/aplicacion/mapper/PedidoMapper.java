package com.javacadabra.tienda.pedidos.aplicacion.mapper;

import com.javacadabra.tienda.pedidos.aplicacion.dto.salida.LineaPedidoDTO;
import com.javacadabra.tienda.pedidos.aplicacion.dto.salida.PedidoDTO;
import com.javacadabra.tienda.pedidos.dominio.modelo.agregado.Pedido;
import com.javacadabra.tienda.pedidos.dominio.modelo.entidad.LineaPedido;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PedidoMapper {

	default PedidoDTO aDTO(Pedido pedido) {
		if (pedido == null) {
			return null;
		}
		return new PedidoDTO(
				pedido.id().valor(),
				pedido.clienteId().valor(),
				pedido.lineas().stream().map(this::aDTO).toList(),
				pedido.total().valor(),
				pedido.fechaCreacion());
	}

	default LineaPedidoDTO aDTO(LineaPedido lineaPedido) {
		if (lineaPedido == null) {
			return null;
		}
		return new LineaPedidoDTO(
				lineaPedido.productoId().valor(),
				lineaPedido.cantidad().valor(),
				lineaPedido.precioUnitario().valor(),
				lineaPedido.subtotal());
	}
}
