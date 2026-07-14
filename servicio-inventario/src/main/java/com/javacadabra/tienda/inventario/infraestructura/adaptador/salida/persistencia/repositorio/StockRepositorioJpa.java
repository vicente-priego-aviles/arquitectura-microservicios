package com.javacadabra.tienda.inventario.infraestructura.adaptador.salida.persistencia.repositorio;

import com.javacadabra.tienda.inventario.infraestructura.adaptador.salida.persistencia.entidad.StockEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepositorioJpa extends JpaRepository<StockEntidad, String> {
}
