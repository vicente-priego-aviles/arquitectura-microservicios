package com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor;

import org.jmolecules.ddd.annotation.ValueObject;

import java.math.BigDecimal;
import java.util.Objects;

@ValueObject
public record LineaPedido(ProductoId productoId, Cantidad cantidad, Precio precioUnitario) {

	public LineaPedido {
		Objects.requireNonNull(productoId, "El producto de la línea de pedido no puede ser nulo");
		Objects.requireNonNull(cantidad, "La cantidad de la línea de pedido no puede ser nula");
		Objects.requireNonNull(precioUnitario, "El precio unitario de la línea de pedido no puede ser nulo");
	}

	public static LineaPedido de(ProductoId productoId, Cantidad cantidad, Precio precioUnitario) {
		return new LineaPedido(productoId, cantidad, precioUnitario);
	}

	public BigDecimal subtotal() {
		return precioUnitario.valor().multiply(BigDecimal.valueOf(cantidad.valor()));
	}
}
