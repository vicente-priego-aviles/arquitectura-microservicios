package com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada;

import com.javacadabra.tienda.catalogo.aplicacion.dto.entrada.RecomendarProductoDTO;
import org.jmolecules.architecture.hexagonal.PrimaryPort;

@PrimaryPort
public interface RecomendarProductoPuertoEntrada {

	void recomendar(String productoId, RecomendarProductoDTO dto);
}
