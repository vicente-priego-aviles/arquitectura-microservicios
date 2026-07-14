package com.javacadabra.tienda.inventario.aplicacion.servicio;

import com.javacadabra.tienda.inventario.aplicacion.puerto.entrada.CrearStockPuertoEntrada;
import com.javacadabra.tienda.inventario.aplicacion.puerto.salida.StockRepositorioPuertoSalida;
import com.javacadabra.tienda.inventario.dominio.modelo.agregado.Stock;
import com.javacadabra.tienda.inventario.dominio.modelo.objetovalor.Cantidad;
import com.javacadabra.tienda.inventario.dominio.modelo.objetovalor.ProductoId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrearStockServicio implements CrearStockPuertoEntrada {

	static final int CANTIDAD_INICIAL = 100;

	private final StockRepositorioPuertoSalida stockRepositorioPuertoSalida;

	@Override
	public void crear(String productoId) {
		ProductoId id = ProductoId.de(productoId);
		if (stockRepositorioPuertoSalida.buscarPorProductoId(id).isPresent()) {
			log.info("Stock ya existente para el producto {}, se descarta el duplicado", productoId);
			return;
		}
		stockRepositorioPuertoSalida.guardar(Stock.crear(id, Cantidad.de(CANTIDAD_INICIAL)));
	}
}
