package com.javacadabra.tienda.pedidos.aplicacion.servicio;

import com.javacadabra.tienda.pedidos.aplicacion.dto.entrada.CrearPedidoDTO;
import com.javacadabra.tienda.pedidos.aplicacion.dto.salida.PedidoDTO;
import com.javacadabra.tienda.pedidos.aplicacion.mapper.PedidoMapper;
import com.javacadabra.tienda.pedidos.aplicacion.puerto.entrada.CrearPedidoPuertoEntrada;
import com.javacadabra.tienda.pedidos.aplicacion.puerto.salida.CatalogoPuertoSalida;
import com.javacadabra.tienda.pedidos.aplicacion.puerto.salida.OutboxPuertoSalida;
import com.javacadabra.tienda.pedidos.aplicacion.puerto.salida.PedidoRepositorioPuertoSalida;
import com.javacadabra.tienda.pedidos.aplicacion.puerto.salida.ProductoCatalogoDTO;
import com.javacadabra.tienda.pedidos.dominio.evento.PedidoCreadoEvento;
import com.javacadabra.tienda.pedidos.dominio.modelo.agregado.Pedido;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.Cantidad;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.ClienteId;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.Precio;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.ProductoId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CrearPedidoServicio implements CrearPedidoPuertoEntrada {

	private final PedidoRepositorioPuertoSalida pedidoRepositorioPuertoSalida;
	private final CatalogoPuertoSalida catalogoPuertoSalida;
	private final OutboxPuertoSalida outboxPuertoSalida;
	private final PedidoMapper pedidoMapper;
	private final TransactionTemplate transactionTemplate;

	@Override
	public PedidoDTO crear(CrearPedidoDTO dto) {
		Pedido pedido = Pedido.crear(ClienteId.de(dto.clienteId()));
		dto.lineas().forEach(linea -> {
			ProductoId productoId = ProductoId.de(linea.productoId());
			ProductoCatalogoDTO producto = catalogoPuertoSalida.buscarProductoPorId(productoId);
			pedido.agregarLinea(productoId, Cantidad.de(linea.cantidad()), Precio.de(producto.precio()));
		});

		// Solo esta parte necesita transacción: las llamadas HTTP a catálogo ya han terminado.
		Pedido guardado = transactionTemplate.execute(status -> {
			Pedido resultado = pedidoRepositorioPuertoSalida.guardar(pedido);
			outboxPuertoSalida.guardar(aEvento(resultado));
			return resultado;
		});
		return pedidoMapper.aDTO(guardado);
	}

	private static PedidoCreadoEvento aEvento(Pedido pedido) {
		var lineas = pedido.lineas().stream()
				.map(linea -> new PedidoCreadoEvento.LineaPedidoCreada(linea.productoId(), linea.cantidad()))
				.toList();
		return new PedidoCreadoEvento(pedido.id(), lineas, Instant.now());
	}
}
