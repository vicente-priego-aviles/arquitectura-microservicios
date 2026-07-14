package com.javacadabra.tienda.pedidos.infraestructura.adaptador.salida.persistencia.repositorio;

import com.javacadabra.tienda.pedidos.infraestructura.adaptador.salida.persistencia.entidad.OutboxEventoEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventoRepositorioJpa extends JpaRepository<OutboxEventoEntidad, Long> {

	List<OutboxEventoEntidad> findByPublicadoFalseOrderByIdAsc();
}
