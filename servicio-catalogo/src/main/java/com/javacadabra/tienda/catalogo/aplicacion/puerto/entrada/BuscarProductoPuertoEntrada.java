package com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada;

import com.javacadabra.tienda.catalogo.aplicacion.dto.salida.ProductoDTO;
import org.jmolecules.architecture.hexagonal.PrimaryPort;

@PrimaryPort
public interface BuscarProductoPuertoEntrada {

	ProductoDTO buscarPorId(String id);
}
