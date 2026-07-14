package com.javacadabra.tienda.inventario.aplicacion.servicio;

import com.javacadabra.tienda.inventario.aplicacion.dto.entrada.LineaReservaDTO;
import com.javacadabra.tienda.inventario.aplicacion.puerto.entrada.ReservarStockPuertoEntrada;
import com.javacadabra.tienda.inventario.aplicacion.puerto.salida.EventoProcesadoPuertoSalida;
import com.javacadabra.tienda.inventario.aplicacion.puerto.salida.StockRepositorioPuertoSalida;
import com.javacadabra.tienda.inventario.dominio.excepcion.StockNoEncontradoException;
import com.javacadabra.tienda.inventario.dominio.modelo.agregado.Stock;
import com.javacadabra.tienda.inventario.dominio.modelo.objetovalor.ProductoId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservarStockServicio implements ReservarStockPuertoEntrada {

	private final StockRepositorioPuertoSalida stockRepositorioPuertoSalida;
	private final EventoProcesadoPuertoSalida eventoProcesadoPuertoSalida;

	@Override
	@Transactional
	public void reservar(String pedidoId, List<LineaReservaDTO> lineas) {
		if (eventoProcesadoPuertoSalida.yaProcesado(pedidoId)) {
			log.info("Pedido {} ya procesado, se descarta el duplicado", pedidoId);
			return;
		}
		lineas.forEach(linea -> {
			ProductoId productoId = ProductoId.de(linea.productoId());
			Stock stock = stockRepositorioPuertoSalida.buscarPorProductoId(productoId)
					.orElseThrow(() -> new StockNoEncontradoException(productoId));
			stock.decrementar(linea.cantidad());
			stockRepositorioPuertoSalida.guardar(stock);
		});
		eventoProcesadoPuertoSalida.marcarProcesado(pedidoId);
	}
}
