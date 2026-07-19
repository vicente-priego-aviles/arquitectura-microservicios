package com.javacadabra.tienda.pedidos.aplicacion.servicio;

import com.javacadabra.tienda.pedidos.aplicacion.dto.entrada.CrearPedidoDTO;
import com.javacadabra.tienda.pedidos.aplicacion.dto.salida.PedidoDTO;
import com.javacadabra.tienda.pedidos.aplicacion.mapper.PedidoMapper;
import com.javacadabra.tienda.pedidos.aplicacion.puerto.entrada.CrearPedidoPuertoEntrada;
import com.javacadabra.tienda.pedidos.aplicacion.puerto.salida.CatalogoPuertoSalida;
import com.javacadabra.tienda.pedidos.aplicacion.puerto.salida.InventarioPuertoSalida;
import com.javacadabra.tienda.pedidos.aplicacion.puerto.salida.PedidoRepositorioPuertoSalida;
import com.javacadabra.tienda.pedidos.aplicacion.puerto.salida.ProductoCatalogoDTO;
import com.javacadabra.tienda.pedidos.dominio.comando.ReservarStockComando;
import com.javacadabra.tienda.pedidos.dominio.modelo.agregado.Pedido;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.Cantidad;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.ClienteId;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.Precio;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.ProductoId;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.ReservaConfirmada;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.ReservaRechazada;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.ResultadoReservaStock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CrearPedidoServicio implements CrearPedidoPuertoEntrada {

	private final PedidoRepositorioPuertoSalida pedidoRepositorioPuertoSalida;
	private final CatalogoPuertoSalida catalogoPuertoSalida;
	private final InventarioPuertoSalida inventarioPuertoSalida;
	private final PedidoMapper pedidoMapper;

	@Override
	public PedidoDTO crear(CrearPedidoDTO dto) {
		Pedido pedido = Pedido.crear(ClienteId.de(dto.clienteId()));
		dto.lineas().forEach(linea -> {
			ProductoId productoId = ProductoId.de(linea.productoId());
			ProductoCatalogoDTO producto = catalogoPuertoSalida.buscarProductoPorId(productoId);
			pedido.agregarLinea(productoId, Cantidad.de(linea.cantidad()), Precio.de(producto.precio()));
		});
		Pedido guardado = pedidoRepositorioPuertoSalida.guardar(pedido);

		// El orquestador de la Saga: llama al siguiente paso explícitamente y, con la
		// respuesta ya en la mano, decide si confirma o compensa cancelando el pedido.
		ResultadoReservaStock resultado = inventarioPuertoSalida.reservarStock(aComando(guardado));
		switch (resultado) {
			case ReservaConfirmada() -> guardado.confirmar();
			case ReservaRechazada rechazada -> guardado.cancelar(rechazada.motivo());
		}

		return pedidoMapper.aDTO(pedidoRepositorioPuertoSalida.guardar(guardado));
	}

	private static ReservarStockComando aComando(Pedido pedido) {
		var lineas = pedido.lineas().stream()
				.map(linea -> new ReservarStockComando.LineaReserva(linea.productoId(), linea.cantidad()))
				.toList();
		return new ReservarStockComando(pedido.id(), lineas);
	}
}
