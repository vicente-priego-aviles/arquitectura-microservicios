package com.javacadabra.tienda.pedidos.aplicacion.puerto.salida;

import java.math.BigDecimal;

public record ProductoCatalogoDTO(String id, String nombre, BigDecimal precio) {
}
