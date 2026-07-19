package com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor;

public sealed interface ResultadoReservaStock permits ReservaConfirmada, ReservaRechazada {
}
