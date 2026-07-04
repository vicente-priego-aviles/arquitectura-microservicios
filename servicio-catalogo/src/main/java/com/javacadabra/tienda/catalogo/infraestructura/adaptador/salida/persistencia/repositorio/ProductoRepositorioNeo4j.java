package com.javacadabra.tienda.catalogo.infraestructura.adaptador.salida.persistencia.repositorio;

import com.javacadabra.tienda.catalogo.infraestructura.adaptador.salida.persistencia.entidad.ProductoEntidad;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface ProductoRepositorioNeo4j extends Neo4jRepository<ProductoEntidad, String> {
}
