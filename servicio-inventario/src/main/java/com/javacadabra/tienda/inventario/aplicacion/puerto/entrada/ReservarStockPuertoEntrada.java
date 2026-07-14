package com.javacadabra.tienda.inventario.aplicacion.puerto.entrada;

import com.javacadabra.tienda.inventario.aplicacion.dto.entrada.LineaReservaDTO;
import org.jmolecules.architecture.hexagonal.PrimaryPort;

import java.util.List;

@PrimaryPort
public interface ReservarStockPuertoEntrada {

	void reservar(String pedidoId, List<LineaReservaDTO> lineas);
}
