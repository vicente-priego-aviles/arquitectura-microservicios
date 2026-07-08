package com.javacadabra.tienda.catalogo.dominio.evento;

import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.ProductoId;

import java.time.Instant;

public record ProductoCreadoEvento(ProductoId productoId, Instant ocurridoEn) {
}
