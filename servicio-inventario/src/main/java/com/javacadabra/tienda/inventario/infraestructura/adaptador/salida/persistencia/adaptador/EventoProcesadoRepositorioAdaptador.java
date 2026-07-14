package com.javacadabra.tienda.inventario.infraestructura.adaptador.salida.persistencia.adaptador;

import com.javacadabra.tienda.inventario.aplicacion.puerto.salida.EventoProcesadoPuertoSalida;
import com.javacadabra.tienda.inventario.infraestructura.adaptador.salida.persistencia.entidad.EventoProcesadoEntidad;
import com.javacadabra.tienda.inventario.infraestructura.adaptador.salida.persistencia.repositorio.EventoProcesadoRepositorioJpa;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.stereotype.Component;

import java.time.Instant;

@SecondaryAdapter
@Component
@RequiredArgsConstructor
public class EventoProcesadoRepositorioAdaptador implements EventoProcesadoPuertoSalida {

	private final EventoProcesadoRepositorioJpa eventoProcesadoRepositorioJpa;

	@Override
	public boolean yaProcesado(String pedidoId) {
		return eventoProcesadoRepositorioJpa.existsById(pedidoId);
	}

	@Override
	public void marcarProcesado(String pedidoId) {
		eventoProcesadoRepositorioJpa.save(new EventoProcesadoEntidad(pedidoId, Instant.now()));
	}
}
