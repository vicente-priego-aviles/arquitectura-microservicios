package com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada;

import com.javacadabra.tienda.catalogo.aplicacion.dto.salida.ProductoDTO;
import org.jmolecules.architecture.hexagonal.PrimaryPort;

import java.util.List;

@PrimaryPort
public interface BuscarProductosRecomendadosPuertoEntrada {

	List<ProductoDTO> buscarRecomendados(String productoId);
}
