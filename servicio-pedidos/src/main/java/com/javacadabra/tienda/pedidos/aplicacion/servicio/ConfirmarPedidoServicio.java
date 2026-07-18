package com.javacadabra.tienda.pedidos.aplicacion.servicio;

import com.javacadabra.tienda.pedidos.aplicacion.puerto.entrada.ConfirmarPedidoPuertoEntrada;
import com.javacadabra.tienda.pedidos.aplicacion.puerto.salida.PedidoRepositorioPuertoSalida;
import com.javacadabra.tienda.pedidos.dominio.excepcion.PedidoNoEncontradoException;
import com.javacadabra.tienda.pedidos.dominio.modelo.agregado.Pedido;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.PedidoId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfirmarPedidoServicio implements ConfirmarPedidoPuertoEntrada {

	private final PedidoRepositorioPuertoSalida pedidoRepositorioPuertoSalida;

	@Override
	@Transactional
	public void confirmar(String pedidoId) {
		Pedido pedido = pedidoRepositorioPuertoSalida.buscarPorId(PedidoId.de(pedidoId))
				.orElseThrow(() -> new PedidoNoEncontradoException(pedidoId));
		pedido.confirmar();
		pedidoRepositorioPuertoSalida.guardar(pedido);
		log.info("Pedido {} confirmado", pedidoId);
	}
}
