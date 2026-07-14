package com.javacadabra.tienda.inventario.infraestructura.adaptador.entrada.mensajeria;

import java.time.Instant;
import java.util.List;

// Traducción propia del PedidoCreadoEvento ajeno (Capa Anticorrupción), no la clase de dominio de servicio-pedidos.
public record PedidoCreadoEventoDTO(PedidoIdDTO pedidoId, List<LineaDTO> lineas, Instant ocurridoEn) {

	public record PedidoIdDTO(String valor) {
	}

	public record LineaDTO(ProductoIdDTO productoId, CantidadDTO cantidad) {
	}

	public record ProductoIdDTO(String valor) {
	}

	public record CantidadDTO(int valor) {
	}
}
