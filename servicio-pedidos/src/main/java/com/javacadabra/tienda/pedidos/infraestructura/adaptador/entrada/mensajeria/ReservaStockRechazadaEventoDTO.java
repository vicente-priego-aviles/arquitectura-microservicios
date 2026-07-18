package com.javacadabra.tienda.pedidos.infraestructura.adaptador.entrada.mensajeria;

import java.time.Instant;

// Traducción propia del ReservaStockRechazadaEvento ajeno (Capa Anticorrupción), no la clase de dominio de servicio-inventario.
public record ReservaStockRechazadaEventoDTO(String pedidoId, String motivo, Instant ocurridoEn) {
}
