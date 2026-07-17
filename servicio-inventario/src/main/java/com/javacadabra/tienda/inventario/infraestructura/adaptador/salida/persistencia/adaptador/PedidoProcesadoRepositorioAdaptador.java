package com.javacadabra.tienda.inventario.infraestructura.adaptador.salida.persistencia.adaptador;

import com.javacadabra.tienda.inventario.aplicacion.puerto.salida.PedidoProcesadoPuertoSalida;
import com.javacadabra.tienda.inventario.infraestructura.adaptador.salida.persistencia.entidad.PedidoProcesadoEntidad;
import com.javacadabra.tienda.inventario.infraestructura.adaptador.salida.persistencia.repositorio.PedidoProcesadoRepositorioJpa;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.stereotype.Component;

import java.time.Instant;

@SecondaryAdapter
@Component
@RequiredArgsConstructor
public class PedidoProcesadoRepositorioAdaptador implements PedidoProcesadoPuertoSalida {

	private final PedidoProcesadoRepositorioJpa pedidoProcesadoRepositorioJpa;

	@Override
	public boolean yaProcesado(String pedidoId) {
		return pedidoProcesadoRepositorioJpa.existsById(pedidoId);
	}

	@Override
	public void marcarProcesado(String pedidoId) {
		pedidoProcesadoRepositorioJpa.save(new PedidoProcesadoEntidad(pedidoId, Instant.now()));
	}
}
