package com.javacadabra.tienda.catalogo.infraestructura.adaptador.entrada.evento;

import com.javacadabra.tienda.catalogo.dominio.evento.RecomendacionAñadidaEvento;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RecomendacionAñadidaListener {

	@EventListener
	public void alAñadirRecomendacion(RecomendacionAñadidaEvento evento) {
		log.info("Recomendación añadida: {} recomienda a {}", evento.productoId().valor(), evento.productoRecomendadoId().valor());
	}
}
