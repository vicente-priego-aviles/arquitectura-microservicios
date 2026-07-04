package com.javacadabra.tienda.catalogo.infraestructura.adaptador.salida.persistencia.entidad;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.math.BigDecimal;
import java.time.Instant;

@Node("Producto")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductoEntidad {

	@Id
	private String id;
	private String nombre;
	private String descripcion;
	private BigDecimal precio;
	private Instant fechaCreacion;
}
