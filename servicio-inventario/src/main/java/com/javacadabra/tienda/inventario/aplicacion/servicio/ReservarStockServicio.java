package com.javacadabra.tienda.inventario.aplicacion.servicio;

import com.javacadabra.tienda.inventario.aplicacion.dto.entrada.LineaReservaDTO;
import com.javacadabra.tienda.inventario.aplicacion.puerto.entrada.ReservarStockPuertoEntrada;
import com.javacadabra.tienda.inventario.aplicacion.puerto.salida.OutboxPuertoSalida;
import com.javacadabra.tienda.inventario.aplicacion.puerto.salida.PedidoProcesadoPuertoSalida;
import com.javacadabra.tienda.inventario.aplicacion.puerto.salida.StockRepositorioPuertoSalida;
import com.javacadabra.tienda.inventario.dominio.evento.ReservaStockRechazadaEvento;
import com.javacadabra.tienda.inventario.dominio.evento.StockReservadoEvento;
import com.javacadabra.tienda.inventario.dominio.excepcion.StockInsuficienteException;
import com.javacadabra.tienda.inventario.dominio.excepcion.StockNoEncontradoException;
import com.javacadabra.tienda.inventario.dominio.modelo.agregado.Stock;
import com.javacadabra.tienda.inventario.dominio.modelo.objetovalor.ProductoId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservarStockServicio implements ReservarStockPuertoEntrada {

	private final StockRepositorioPuertoSalida stockRepositorioPuertoSalida;
	private final PedidoProcesadoPuertoSalida pedidoProcesadoPuertoSalida;
	private final OutboxPuertoSalida outboxPuertoSalida;
	private final TransactionTemplate transactionTemplate;

	@Override
	public void reservar(String pedidoId, List<LineaReservaDTO> lineas) {
		if (pedidoProcesadoPuertoSalida.yaProcesado(pedidoId)) {
			log.info("Pedido {} ya procesado, se descarta el duplicado", pedidoId);
			return;
		}
		try {
			transactionTemplate.executeWithoutResult(status -> {
				lineas.forEach(linea -> {
					ProductoId productoId = ProductoId.de(linea.productoId());
					Stock stock = stockRepositorioPuertoSalida.buscarPorProductoId(productoId)
							.orElseThrow(() -> new StockNoEncontradoException(productoId));
					stock.decrementar(linea.cantidad());
					stockRepositorioPuertoSalida.guardar(stock);
				});
				pedidoProcesadoPuertoSalida.marcarProcesado(pedidoId);
				outboxPuertoSalida.guardar(new StockReservadoEvento(pedidoId, Instant.now()));
			});
		} catch (StockInsuficienteException | StockNoEncontradoException e) {
			log.info("Reserva de stock rechazada para el pedido {}: {}", pedidoId, e.getMessage());
			transactionTemplate.executeWithoutResult(status -> {
				pedidoProcesadoPuertoSalida.marcarProcesado(pedidoId);
				outboxPuertoSalida.guardar(new ReservaStockRechazadaEvento(pedidoId, e.getMessage(), Instant.now()));
			});
		}
	}
}
