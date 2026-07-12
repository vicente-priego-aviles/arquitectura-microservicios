package com.javacadabra.tienda.catalogo.aplicacion.puerto.salida;

import com.javacadabra.tienda.catalogo.dominio.modelo.agregado.Categoria;
import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.CategoriaId;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.ddd.annotation.Repository;

import java.util.Optional;

@Repository
@SecondaryPort
public interface CategoriaRepositorioPuertoSalida {

	Categoria guardar(Categoria categoria);

	Optional<Categoria> buscarPorId(CategoriaId id);
}
