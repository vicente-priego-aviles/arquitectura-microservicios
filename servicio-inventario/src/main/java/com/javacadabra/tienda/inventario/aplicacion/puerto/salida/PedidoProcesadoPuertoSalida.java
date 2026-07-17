package com.javacadabra.tienda.inventario.aplicacion.puerto.salida;

import org.jmolecules.architecture.hexagonal.SecondaryPort;

@SecondaryPort
public interface PedidoProcesadoPuertoSalida {

	boolean yaProcesado(String pedidoId);

	void marcarProcesado(String pedidoId);
}
