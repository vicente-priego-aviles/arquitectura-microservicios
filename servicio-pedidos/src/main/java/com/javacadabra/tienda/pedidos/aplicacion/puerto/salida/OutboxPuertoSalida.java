package com.javacadabra.tienda.pedidos.aplicacion.puerto.salida;

import com.javacadabra.tienda.pedidos.dominio.evento.PedidoCreadoEvento;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

@SecondaryPort
public interface OutboxPuertoSalida {

	void guardar(PedidoCreadoEvento evento);
}
