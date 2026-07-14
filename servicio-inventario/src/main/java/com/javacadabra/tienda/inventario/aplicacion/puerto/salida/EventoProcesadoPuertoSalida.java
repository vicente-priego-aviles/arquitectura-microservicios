package com.javacadabra.tienda.inventario.aplicacion.puerto.salida;

import org.jmolecules.architecture.hexagonal.SecondaryPort;

@SecondaryPort
public interface EventoProcesadoPuertoSalida {

	boolean yaProcesado(String pedidoId);

	void marcarProcesado(String pedidoId);
}
