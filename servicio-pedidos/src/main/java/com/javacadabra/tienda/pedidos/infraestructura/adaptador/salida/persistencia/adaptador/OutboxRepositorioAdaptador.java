package com.javacadabra.tienda.pedidos.infraestructura.adaptador.salida.persistencia.adaptador;

import com.javacadabra.tienda.pedidos.aplicacion.puerto.salida.OutboxPuertoSalida;
import com.javacadabra.tienda.pedidos.dominio.evento.PedidoCreadoEvento;
import com.javacadabra.tienda.pedidos.infraestructura.adaptador.salida.persistencia.entidad.OutboxEventoEntidad;
import com.javacadabra.tienda.pedidos.infraestructura.adaptador.salida.persistencia.repositorio.OutboxEventoRepositorioJpa;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@SecondaryAdapter
@Component
@RequiredArgsConstructor
public class OutboxRepositorioAdaptador implements OutboxPuertoSalida {

	private final OutboxEventoRepositorioJpa outboxEventoRepositorioJpa;
	private final JsonMapper jsonMapper;

	@Override
	public void guardar(PedidoCreadoEvento evento) {
		String payload = jsonMapper.writeValueAsString(evento);
		outboxEventoRepositorioJpa.save(new OutboxEventoEntidad(
				null, PedidoCreadoEvento.class.getSimpleName(), payload, evento.ocurridoEn(), false));
	}
}
