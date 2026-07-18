package com.javacadabra.tienda.inventario.dominio.evento;

import java.time.Instant;

public record ReservaStockRechazadaEvento(String pedidoId, String motivo, Instant ocurridoEn) {
}
