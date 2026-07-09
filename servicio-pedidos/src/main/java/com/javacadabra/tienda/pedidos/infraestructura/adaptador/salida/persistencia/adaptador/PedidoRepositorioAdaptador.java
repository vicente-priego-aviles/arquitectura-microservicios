package com.javacadabra.tienda.pedidos.infraestructura.adaptador.salida.persistencia.adaptador;

import com.javacadabra.tienda.pedidos.aplicacion.puerto.salida.PedidoRepositorioPuertoSalida;
import com.javacadabra.tienda.pedidos.dominio.modelo.agregado.Pedido;
import com.javacadabra.tienda.pedidos.infraestructura.adaptador.salida.persistencia.mapper.PedidoEntidadMapper;
import com.javacadabra.tienda.pedidos.infraestructura.adaptador.salida.persistencia.repositorio.PedidoRepositorioJpa;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PedidoRepositorioAdaptador implements PedidoRepositorioPuertoSalida {

	private final PedidoRepositorioJpa pedidoRepositorioJpa;
	private final PedidoEntidadMapper pedidoEntidadMapper;

	@Override
	public Pedido guardar(Pedido pedido) {
		var entidadGuardada = pedidoRepositorioJpa.save(pedidoEntidadMapper.aEntidad(pedido));
		return pedidoEntidadMapper.aDominio(entidadGuardada);
	}
}
