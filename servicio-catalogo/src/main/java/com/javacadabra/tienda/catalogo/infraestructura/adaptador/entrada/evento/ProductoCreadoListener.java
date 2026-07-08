package com.javacadabra.tienda.catalogo.infraestructura.adaptador.entrada.evento;

import com.javacadabra.tienda.catalogo.dominio.evento.ProductoCreadoEvento;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProductoCreadoListener {

	@EventListener
	public void alCrearProducto(ProductoCreadoEvento evento) {
		log.info("Producto creado: {}", evento.productoId().valor());
	}
}
