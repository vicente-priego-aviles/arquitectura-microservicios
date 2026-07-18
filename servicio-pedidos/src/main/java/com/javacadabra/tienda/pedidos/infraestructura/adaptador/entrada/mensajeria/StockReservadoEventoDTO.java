package com.javacadabra.tienda.pedidos.infraestructura.adaptador.entrada.mensajeria;

import java.time.Instant;

// Traducción propia del StockReservadoEvento ajeno (Capa Anticorrupción), no la clase de dominio de servicio-inventario.
public record StockReservadoEventoDTO(String pedidoId, Instant ocurridoEn) {
}
