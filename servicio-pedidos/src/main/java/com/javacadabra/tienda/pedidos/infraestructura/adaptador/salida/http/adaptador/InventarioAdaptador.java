package com.javacadabra.tienda.pedidos.infraestructura.adaptador.salida.http.adaptador;

import com.javacadabra.tienda.pedidos.aplicacion.puerto.salida.InventarioPuertoSalida;
import com.javacadabra.tienda.pedidos.dominio.comando.ReservarStockComando;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.ReservaConfirmada;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.ReservaRechazada;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.ResultadoReservaStock;
import com.javacadabra.tienda.pedidos.infraestructura.adaptador.salida.http.cliente.InventarioHttpExchange;
import com.javacadabra.tienda.pedidos.infraestructura.adaptador.salida.http.cliente.ReservaStockPeticion;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

@SecondaryAdapter
@Component
@RequiredArgsConstructor
public class InventarioAdaptador implements InventarioPuertoSalida {

	private final InventarioHttpExchange inventarioHttpExchange;

	@Override
	public ResultadoReservaStock reservarStock(ReservarStockComando comando) {
		try {
			inventarioHttpExchange.reservarStock(aPeticion(comando));
			return new ReservaConfirmada();
		} catch (HttpClientErrorException.Conflict | HttpClientErrorException.NotFound excepcion) {
			return new ReservaRechazada(extraerMotivo(excepcion));
		}
	}

	private static ReservaStockPeticion aPeticion(ReservarStockComando comando) {
		var lineas = comando.lineas().stream()
				.map(linea -> new ReservaStockPeticion.LineaReservaPeticion(linea.productoId().valor(), linea.cantidad().valor()))
				.toList();
		return new ReservaStockPeticion(comando.pedidoId().valor(), lineas);
	}

	private static String extraerMotivo(HttpClientErrorException excepcion) {
		ProblemDetail problema = excepcion.getResponseBodyAs(ProblemDetail.class);
		return problema != null ? problema.getDetail() : excepcion.getMessage();
	}
}
