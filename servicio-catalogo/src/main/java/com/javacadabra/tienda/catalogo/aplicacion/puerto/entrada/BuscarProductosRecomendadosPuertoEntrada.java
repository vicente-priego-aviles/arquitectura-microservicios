package com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada;

import com.javacadabra.tienda.catalogo.aplicacion.dto.salida.ProductoDTO;

import java.util.List;

public interface BuscarProductosRecomendadosPuertoEntrada {

	List<ProductoDTO> buscarRecomendados(String productoId);
}
