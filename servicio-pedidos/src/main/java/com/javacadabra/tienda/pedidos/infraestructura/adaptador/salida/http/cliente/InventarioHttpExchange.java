package com.javacadabra.tienda.pedidos.infraestructura.adaptador.salida.http.cliente;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

public interface InventarioHttpExchange {

	@PostExchange("/api/v1/reservas-stock")
	void reservarStock(@RequestBody ReservaStockPeticion peticion);
}
