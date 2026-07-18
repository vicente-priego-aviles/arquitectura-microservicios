package com.javacadabra.tienda.pedidos.aplicacion.puerto.entrada;

import org.jmolecules.architecture.hexagonal.PrimaryPort;

@PrimaryPort
public interface CancelarPedidoPuertoEntrada {

	void cancelar(String pedidoId, String motivo);
}
