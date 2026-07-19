package com.javacadabra.tienda.pedidos.dominio.modelo.agregado;

import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.Cantidad;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.ClienteId;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.EstadoPedido;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.LineaPedido;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.PedidoId;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.Precio;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.ProductoId;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jmolecules.ddd.annotation.AggregateRoot;
import org.jmolecules.ddd.annotation.Identity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@AggregateRoot
@Getter
@Accessors(fluent = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Pedido {

	@Identity
	@EqualsAndHashCode.Include
	private final PedidoId id;
	private final ClienteId clienteId;
	private final List<LineaPedido> lineas;
	private final Instant fechaCreacion;
	private EstadoPedido estado;
	private String motivoCancelacion;

	private Pedido(PedidoId id, ClienteId clienteId, List<LineaPedido> lineas, Instant fechaCreacion,
			EstadoPedido estado, String motivoCancelacion) {
		this.id = id;
		this.clienteId = clienteId;
		this.lineas = lineas;
		this.fechaCreacion = fechaCreacion;
		this.estado = estado;
		this.motivoCancelacion = motivoCancelacion;
	}

	public static Pedido crear(ClienteId clienteId) {
		Objects.requireNonNull(clienteId, "El cliente del pedido no puede ser nulo");
		return new Pedido(PedidoId.generar(), clienteId, new ArrayList<>(), Instant.now(),
				EstadoPedido.PENDIENTE_CONFIRMACION, null);
	}

	public static Pedido reconstruir(PedidoId id, ClienteId clienteId, List<LineaPedido> lineas, Instant fechaCreacion,
			EstadoPedido estado, String motivoCancelacion) {
		return new Pedido(id, clienteId, new ArrayList<>(lineas), fechaCreacion, estado, motivoCancelacion);
	}

	public void agregarLinea(ProductoId productoId, Cantidad cantidad, Precio precioUnitario) {
		lineas.add(LineaPedido.de(productoId, cantidad, precioUnitario));
	}

	public void confirmar() {
		if (estado == EstadoPedido.CONFIRMADO) {
			return;
		}
		this.estado = EstadoPedido.CONFIRMADO;
	}

	public void cancelar(String motivo) {
		if (estado == EstadoPedido.CANCELADO) {
			return;
		}
		this.estado = EstadoPedido.CANCELADO;
		this.motivoCancelacion = motivo;
	}

	public List<LineaPedido> lineas() {
		return List.copyOf(lineas);
	}

	public Precio total() {
		BigDecimal total = lineas.stream()
				.map(LineaPedido::subtotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		return Precio.de(total);
	}
}
