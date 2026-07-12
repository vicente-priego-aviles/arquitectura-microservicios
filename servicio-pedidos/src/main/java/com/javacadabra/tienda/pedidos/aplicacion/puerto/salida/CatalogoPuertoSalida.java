package com.javacadabra.tienda.pedidos.aplicacion.puerto.salida;

import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.ProductoId;
import org.jmolecules.architecture.hexagonal.SecondaryPort;

@SecondaryPort
public interface CatalogoPuertoSalida {

	ProductoCatalogoDTO buscarProductoPorId(ProductoId productoId);
}
