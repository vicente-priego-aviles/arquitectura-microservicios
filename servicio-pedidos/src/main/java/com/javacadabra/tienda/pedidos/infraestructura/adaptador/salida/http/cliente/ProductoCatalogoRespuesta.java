package com.javacadabra.tienda.pedidos.infraestructura.adaptador.salida.http.cliente;

import java.math.BigDecimal;

public record ProductoCatalogoRespuesta(String id, String nombre, String descripcion, BigDecimal precio, String categoriaId) {
}
