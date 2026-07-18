package com.javacadabra.tienda.inventario.aplicacion.puerto.salida;

import com.javacadabra.tienda.inventario.dominio.evento.ReservaStockRechazadaEvento;
import com.javacadabra.tienda.inventario.dominio.evento.StockReservadoEvento;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

@SecondaryPort
public interface OutboxPuertoSalida {

	void guardar(StockReservadoEvento evento);

	void guardar(ReservaStockRechazadaEvento evento);
}
