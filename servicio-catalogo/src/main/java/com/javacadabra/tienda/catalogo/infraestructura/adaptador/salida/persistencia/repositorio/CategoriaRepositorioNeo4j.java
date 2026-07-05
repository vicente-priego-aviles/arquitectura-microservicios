package com.javacadabra.tienda.catalogo.infraestructura.adaptador.salida.persistencia.repositorio;

import com.javacadabra.tienda.catalogo.infraestructura.adaptador.salida.persistencia.entidad.CategoriaEntidad;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface CategoriaRepositorioNeo4j extends Neo4jRepository<CategoriaEntidad, String> {
}
