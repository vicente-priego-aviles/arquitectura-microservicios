package com.javacadabra.tienda.catalogo.dominio.modelo.agregado;

import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.Precio;
import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.ProductoId;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.Objects;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Producto {

	@EqualsAndHashCode.Include
	private final ProductoId id;
	private String nombre;
	private String descripcion;
	private Precio precio;
	private final Instant fechaCreacion;

	private Producto(ProductoId id, String nombre, String descripcion, Precio precio, Instant fechaCreacion) {
		this.id = id;
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.precio = precio;
		this.fechaCreacion = fechaCreacion;
	}

	public static Producto crear(String nombre, String descripcion, Precio precio) {
		validarNombre(nombre);
		Objects.requireNonNull(precio, "El precio no puede ser nulo");
		return new Producto(ProductoId.generar(), nombre, descripcion, precio, Instant.now());
	}

	public static Producto reconstruir(ProductoId id, String nombre, String descripcion, Precio precio, Instant fechaCreacion) {
		return new Producto(id, nombre, descripcion, precio, fechaCreacion);
	}

	private static void validarNombre(String nombre) {
		if (nombre == null || nombre.isBlank()) {
			throw new IllegalArgumentException("El nombre del producto no puede estar vacío");
		}
	}

	public ProductoId id() {
		return id;
	}

	public String nombre() {
		return nombre;
	}

	public String descripcion() {
		return descripcion;
	}

	public Precio precio() {
		return precio;
	}

	public Instant fechaCreacion() {
		return fechaCreacion;
	}
}
