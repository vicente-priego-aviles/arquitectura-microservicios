package com.javacadabra.tienda.catalogo.infraestructura.adaptador.entrada.evento;

import com.javacadabra.tienda.catalogo.dominio.evento.ProductoCreadoEvento;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductoCreadoListener {

	private final StreamBridge streamBridge;

	@EventListener
	public void alCrearProducto(ProductoCreadoEvento evento) {
		streamBridge.send("productoCreado-out-0", evento);
	}
}
