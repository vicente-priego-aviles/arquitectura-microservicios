package com.javacadabra.tienda.catalogo.aplicacion.puerto.salida;

import com.javacadabra.tienda.catalogo.dominio.modelo.agregado.Categoria;
import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.CategoriaId;

import java.util.Optional;

public interface CategoriaRepositorioPuertoSalida {

	Categoria guardar(Categoria categoria);

	Optional<Categoria> buscarPorId(CategoriaId id);
}
