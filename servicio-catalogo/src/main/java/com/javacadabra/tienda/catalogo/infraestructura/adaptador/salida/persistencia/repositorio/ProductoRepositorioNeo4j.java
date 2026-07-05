package com.javacadabra.tienda.catalogo.infraestructura.adaptador.salida.persistencia.repositorio;

import com.javacadabra.tienda.catalogo.infraestructura.adaptador.salida.persistencia.entidad.ProductoEntidad;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProductoRepositorioNeo4j extends Neo4jRepository<ProductoEntidad, String> {

	@Transactional(readOnly = true)
	@Query("MATCH (p:Producto)-[r:PERTENECE_A]->(c:Categoria {id: $categoriaId}) RETURN p, collect(r), collect(c)")
	List<ProductoEntidad> buscarPorCategoriaId(@Param("categoriaId") String categoriaId);

	@Transactional(readOnly = true)
	@Query("MATCH (:Producto {id: $productoId})-[:RELACIONADO_CON]->(rec:Producto)-[r:PERTENECE_A]->(c:Categoria) RETURN rec, collect(r), collect(c)")
	List<ProductoEntidad> buscarRecomendados(@Param("productoId") String productoId);

	@Query("MATCH (p:Producto {id: $productoId}), (rec:Producto {id: $recomendadoId}) MERGE (p)-[:RELACIONADO_CON]->(rec)")
	void agregarRecomendacion(@Param("productoId") String productoId, @Param("recomendadoId") String recomendadoId);
}
