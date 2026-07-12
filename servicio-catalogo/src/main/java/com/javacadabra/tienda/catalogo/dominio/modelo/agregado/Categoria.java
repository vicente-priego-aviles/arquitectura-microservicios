package com.javacadabra.tienda.catalogo.dominio.modelo.agregado;

import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.CategoriaId;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

@AggregateRoot
@Getter
@Accessors(fluent = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Categoria {

	@Identity
	@EqualsAndHashCode.Include
	private final CategoriaId id;
	private String nombre;

	private Categoria(CategoriaId id, String nombre) {
		this.id = id;
		this.nombre = nombre;
	}

	public static Categoria crear(String nombre) {
		validarNombre(nombre);
		return new Categoria(CategoriaId.generar(), nombre);
	}

	public static Categoria reconstruir(CategoriaId id, String nombre) {
		return new Categoria(id, nombre);
	}

	private static void validarNombre(String nombre) {
		if (nombre == null || nombre.isBlank()) {
			throw new IllegalArgumentException("El nombre de la categoría no puede estar vacío");
		}
	}
}
