package com.javacadabra.tienda.inventario.infraestructura.adaptador.salida.persistencia.repositorio;

import com.javacadabra.tienda.inventario.infraestructura.adaptador.salida.persistencia.entidad.PedidoProcesadoEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoProcesadoRepositorioJpa extends JpaRepository<PedidoProcesadoEntidad, String> {
}
