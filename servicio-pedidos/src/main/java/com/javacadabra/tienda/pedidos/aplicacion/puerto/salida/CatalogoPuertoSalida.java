package com.javacadabra.tienda.pedidos.aplicacion.puerto.salida;

import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.ProductoId;

public interface CatalogoPuertoSalida {

	ProductoCatalogoDTO buscarProductoPorId(ProductoId productoId);
}
