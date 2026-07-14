package com.javacadabra.tienda.inventario.infraestructura.adaptador.salida.persistencia.repositorio;

import com.javacadabra.tienda.inventario.infraestructura.adaptador.salida.persistencia.entidad.EventoProcesadoEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoProcesadoRepositorioJpa extends JpaRepository<EventoProcesadoEntidad, String> {
}
