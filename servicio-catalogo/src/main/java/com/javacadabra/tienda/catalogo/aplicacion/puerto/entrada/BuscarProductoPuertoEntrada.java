package com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada;

import com.javacadabra.tienda.catalogo.aplicacion.dto.salida.ProductoDTO;

public interface BuscarProductoPuertoEntrada {

	ProductoDTO buscarPorId(String id);
}
