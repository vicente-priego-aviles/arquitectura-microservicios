package com.javacadabra.tienda.catalogo.infraestructura.adaptador.salida.persistencia.entidad;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node("Categoria")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaEntidad {

	@Id
	private String id;
	private String nombre;
}
