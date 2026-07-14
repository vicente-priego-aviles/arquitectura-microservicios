package com.javacadabra.tienda.inventario.dominio.modelo.agregado;

import com.javacadabra.tienda.inventario.dominio.excepcion.StockInsuficienteException;
import com.javacadabra.tienda.inventario.dominio.modelo.objetovalor.Cantidad;
import com.javacadabra.tienda.inventario.dominio.modelo.objetovalor.ProductoId;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

import java.util.Objects;

@AggregateRoot
@Getter
@Accessors(fluent = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Stock {

	@Identity
	@EqualsAndHashCode.Include
	private final ProductoId productoId;
	private Cantidad cantidad;

	private Stock(ProductoId productoId, Cantidad cantidad) {
		this.productoId = productoId;
		this.cantidad = cantidad;
	}

	public static Stock crear(ProductoId productoId, Cantidad cantidadInicial) {
		Objects.requireNonNull(productoId, "El producto del stock no puede ser nulo");
		Objects.requireNonNull(cantidadInicial, "La cantidad inicial de stock no puede ser nula");
		return new Stock(productoId, cantidadInicial);
	}

	public static Stock reconstruir(ProductoId productoId, Cantidad cantidad) {
		return new Stock(productoId, cantidad);
	}

	public void decrementar(int cantidadADecrementar) {
		if (cantidadADecrementar > cantidad.valor()) {
			throw new StockInsuficienteException(productoId, cantidad.valor(), cantidadADecrementar);
		}
		this.cantidad = Cantidad.de(cantidad.valor() - cantidadADecrementar);
	}
}
