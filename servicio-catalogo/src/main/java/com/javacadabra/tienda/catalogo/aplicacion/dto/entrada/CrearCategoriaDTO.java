package com.javacadabra.tienda.catalogo.aplicacion.dto.entrada;

import io.swagger.v3.oas.annotations.media.Schema;

public record CrearCategoriaDTO(@Schema(example = "Ropa") String nombre) {
}
