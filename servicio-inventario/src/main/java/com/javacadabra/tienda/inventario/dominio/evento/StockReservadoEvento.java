package com.javacadabra.tienda.inventario.dominio.evento;

import java.time.Instant;

public record StockReservadoEvento(String pedidoId, Instant ocurridoEn) {
}
