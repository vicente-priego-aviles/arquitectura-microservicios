package com.javacadabra.tienda.inventario.infraestructura.adaptador.entrada.mensajeria;

import java.time.Instant;

// Traducción propia del ProductoCreadoEvento ajeno (Capa Anticorrupción), no la clase de dominio de servicio-catalogo.
public record ProductoCreadoEventoDTO(ProductoIdDTO productoId, Instant ocurridoEn) {

	public record ProductoIdDTO(String valor) {
	}
}
