package com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada;

import com.javacadabra.tienda.catalogo.aplicacion.dto.entrada.CrearCategoriaDTO;
import com.javacadabra.tienda.catalogo.aplicacion.dto.salida.CategoriaDTO;

public interface CrearCategoriaPuertoEntrada {

	CategoriaDTO crear(CrearCategoriaDTO dto);
}
