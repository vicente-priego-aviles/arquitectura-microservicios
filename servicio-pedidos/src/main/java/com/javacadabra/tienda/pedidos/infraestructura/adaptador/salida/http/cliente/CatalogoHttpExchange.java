package com.javacadabra.tienda.pedidos.infraestructura.adaptador.salida.http.cliente;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

public interface CatalogoHttpExchange {

	@GetExchange("/api/productos/{id}")
	ProductoCatalogoRespuesta buscarProductoPorId(@PathVariable String id);
}
