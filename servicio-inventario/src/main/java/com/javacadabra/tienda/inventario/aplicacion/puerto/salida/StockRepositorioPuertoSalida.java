package com.javacadabra.tienda.inventario.aplicacion.puerto.salida;

import com.javacadabra.tienda.inventario.dominio.modelo.agregado.Stock;
import com.javacadabra.tienda.inventario.dominio.modelo.objetovalor.ProductoId;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.ddd.annotation.Repository;

import java.util.Optional;

@Repository
@SecondaryPort
public interface StockRepositorioPuertoSalida {

	Stock guardar(Stock stock);

	Optional<Stock> buscarPorProductoId(ProductoId productoId);
}
