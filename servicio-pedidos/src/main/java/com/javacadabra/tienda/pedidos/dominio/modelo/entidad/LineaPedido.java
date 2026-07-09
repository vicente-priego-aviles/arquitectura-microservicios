package com.javacadabra.tienda.pedidos.dominio.modelo.entidad;

import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.Cantidad;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.Precio;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.ProductoId;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
@Accessors(fluent = true)
public class LineaPedido {

	private final ProductoId productoId;
	private final Cantidad cantidad;
	private final Precio precioUnitario;

	private LineaPedido(ProductoId productoId, Cantidad cantidad, Precio precioUnitario) {
		this.productoId = productoId;
		this.cantidad = cantidad;
		this.precioUnitario = precioUnitario;
	}

	public static LineaPedido crear(ProductoId productoId, Cantidad cantidad, Precio precioUnitario) {
		Objects.requireNonNull(productoId, "El producto de la línea de pedido no puede ser nulo");
		Objects.requireNonNull(cantidad, "La cantidad de la línea de pedido no puede ser nula");
		Objects.requireNonNull(precioUnitario, "El precio unitario de la línea de pedido no puede ser nulo");
		return new LineaPedido(productoId, cantidad, precioUnitario);
	}

	public BigDecimal subtotal() {
		return precioUnitario.valor().multiply(BigDecimal.valueOf(cantidad.valor()));
	}
}
