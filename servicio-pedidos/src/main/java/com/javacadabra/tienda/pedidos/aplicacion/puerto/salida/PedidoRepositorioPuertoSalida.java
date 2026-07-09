package com.javacadabra.tienda.pedidos.aplicacion.puerto.salida;

import com.javacadabra.tienda.pedidos.dominio.modelo.agregado.Pedido;

public interface PedidoRepositorioPuertoSalida {

	Pedido guardar(Pedido pedido);
}
