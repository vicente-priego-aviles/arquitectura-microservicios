package com.javacadabra.tienda.pedidos.aplicacion.servicio;

import com.javacadabra.tienda.pedidos.aplicacion.puerto.entrada.CancelarPedidoPuertoEntrada;
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
public class CancelarPedidoServicio implements CancelarPedidoPuertoEntrada {

	private final PedidoRepositorioPuertoSalida pedidoRepositorioPuertoSalida;

	@Override
	@Transactional
	public void cancelar(String pedidoId, String motivo) {
		Pedido pedido = pedidoRepositorioPuertoSalida.buscarPorId(PedidoId.de(pedidoId))
				.orElseThrow(() -> new PedidoNoEncontradoException(pedidoId));
		pedido.cancelar(motivo);
		pedidoRepositorioPuertoSalida.guardar(pedido);
		log.info("Pedido {} cancelado: {}", pedidoId, motivo);
	}
}
