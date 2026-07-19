package com.javacadabra.tienda.inventario.aplicacion.dto.entrada;

import java.util.List;

public record ReservaStockPeticionDTO(String pedidoId, List<LineaReservaDTO> lineas) {
}
