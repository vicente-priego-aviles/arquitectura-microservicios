package com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada;

import com.javacadabra.tienda.catalogo.aplicacion.dto.salida.CategoriaDTO;

public interface BuscarCategoriaPuertoEntrada {

	CategoriaDTO buscarPorId(String id);
}
