package com.javacadabra.tienda.pedidos.infraestructura.adaptador.salida.persistencia.repositorio;

import com.javacadabra.tienda.pedidos.infraestructura.adaptador.salida.persistencia.entidad.PedidoEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepositorioJpa extends JpaRepository<PedidoEntidad, String> {
}
