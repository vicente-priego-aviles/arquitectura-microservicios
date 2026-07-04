package com.javacadabra.tienda.catalogo.infraestructura.adaptador.salida.persistencia.entidad;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.math.BigDecimal;
import java.time.Instant;

@Node("Producto")
public class ProductoEntidad {

	@Id
	private String id;
	private String nombre;
	private String descripcion;
	private BigDecimal precio;
	private Instant fechaCreacion;

	public ProductoEntidad() {
	}

	public ProductoEntidad(String id, String nombre, String descripcion, BigDecimal precio, Instant fechaCreacion) {
		this.id = id;
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.precio = precio;
		this.fechaCreacion = fechaCreacion;
	}

	public String getId() {
		return id;
	}

	public String getNombre() {
		return nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public BigDecimal getPrecio() {
		return precio;
	}

	public Instant getFechaCreacion() {
		return fechaCreacion;
	}
}
