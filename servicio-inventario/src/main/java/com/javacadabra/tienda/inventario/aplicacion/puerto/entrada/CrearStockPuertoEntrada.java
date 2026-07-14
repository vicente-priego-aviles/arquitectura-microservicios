package com.javacadabra.tienda.inventario.aplicacion.puerto.entrada;

import org.jmolecules.architecture.hexagonal.PrimaryPort;

@PrimaryPort
public interface CrearStockPuertoEntrada {

	void crear(String productoId);
}
