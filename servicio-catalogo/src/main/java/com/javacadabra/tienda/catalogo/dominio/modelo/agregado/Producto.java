package com.javacadabra.tienda.catalogo.dominio.modelo.agregado;

import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.CategoriaId;
import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.Precio;
import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.ProductoId;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.Objects;

@Getter
@Accessors(fluent = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Producto {

	@EqualsAndHashCode.Include
	private final ProductoId id;
	private String nombre;
	private String descripcion;
	private Precio precio;
	private CategoriaId categoriaId;
	private final Instant fechaCreacion;

	private Producto(ProductoId id, String nombre, String descripcion, Precio precio, CategoriaId categoriaId, Instant fechaCreacion) {
		this.id = id;
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.precio = precio;
		this.categoriaId = categoriaId;
		this.fechaCreacion = fechaCreacion;
	}

	public static Producto crear(String nombre, String descripcion, Precio precio, CategoriaId categoriaId) {
		validarNombre(nombre);
		Objects.requireNonNull(precio, "El precio no puede ser nulo");
		Objects.requireNonNull(categoriaId, "La categoría del producto no puede ser nula");
		return new Producto(ProductoId.generar(), nombre, descripcion, precio, categoriaId, Instant.now());
	}

	public static Producto reconstruir(ProductoId id, String nombre, String descripcion, Precio precio, CategoriaId categoriaId, Instant fechaCreacion) {
		return new Producto(id, nombre, descripcion, precio, categoriaId, fechaCreacion);
	}

	public void validarRecomendacion(ProductoId productoRecomendadoId) {
		if (this.id.equals(productoRecomendadoId)) {
			throw new IllegalArgumentException("Un producto no puede recomendarse a sí mismo");
		}
	}

	private static void validarNombre(String nombre) {
		if (nombre == null || nombre.isBlank()) {
			throw new IllegalArgumentException("El nombre del producto no puede estar vacío");
		}
	}
}
