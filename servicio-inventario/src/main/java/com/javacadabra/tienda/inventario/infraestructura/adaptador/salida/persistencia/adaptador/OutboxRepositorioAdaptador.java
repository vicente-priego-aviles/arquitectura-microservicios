package com.javacadabra.tienda.inventario.infraestructura.adaptador.salida.persistencia.adaptador;

import com.javacadabra.tienda.inventario.aplicacion.puerto.salida.OutboxPuertoSalida;
import com.javacadabra.tienda.inventario.dominio.evento.ReservaStockRechazadaEvento;
import com.javacadabra.tienda.inventario.dominio.evento.StockReservadoEvento;
import com.javacadabra.tienda.inventario.infraestructura.adaptador.salida.persistencia.entidad.OutboxEventoEntidad;
import com.javacadabra.tienda.inventario.infraestructura.adaptador.salida.persistencia.repositorio.OutboxEventoRepositorioJpa;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;

@SecondaryAdapter
@Component
@RequiredArgsConstructor
public class OutboxRepositorioAdaptador implements OutboxPuertoSalida {

	private final OutboxEventoRepositorioJpa outboxEventoRepositorioJpa;
	private final JsonMapper jsonMapper;

	@Override
	public void guardar(StockReservadoEvento evento) {
		guardar(evento.getClass().getSimpleName(), evento, evento.ocurridoEn());
	}

	@Override
	public void guardar(ReservaStockRechazadaEvento evento) {
		guardar(evento.getClass().getSimpleName(), evento, evento.ocurridoEn());
	}

	private void guardar(String tipoEvento, Object evento, Instant ocurridoEn) {
		String payload = jsonMapper.writeValueAsString(evento);
		outboxEventoRepositorioJpa.save(new OutboxEventoEntidad(null, tipoEvento, payload, ocurridoEn, false));
	}
}
