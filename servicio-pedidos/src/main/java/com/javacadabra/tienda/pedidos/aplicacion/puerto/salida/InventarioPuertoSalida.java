package com.javacadabra.tienda.pedidos.aplicacion.puerto.salida;

import com.javacadabra.tienda.pedidos.dominio.comando.ReservarStockComando;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.ResultadoReservaStock;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

@SecondaryPort
public interface InventarioPuertoSalida {

	ResultadoReservaStock reservarStock(ReservarStockComando comando);
}
