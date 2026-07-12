package com.javacadabra.tienda.pedidos.aplicacion.puerto.salida;

import com.javacadabra.tienda.pedidos.dominio.modelo.agregado.Pedido;
import org.jmolecules.architecture.hexagonal.SecondaryPort;
import org.jmolecules.ddd.annotation.Repository;

@Repository
@SecondaryPort
public interface PedidoRepositorioPuertoSalida {

	Pedido guardar(Pedido pedido);
}
