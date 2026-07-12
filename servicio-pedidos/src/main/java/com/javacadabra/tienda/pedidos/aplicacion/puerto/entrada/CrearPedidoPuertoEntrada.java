package com.javacadabra.tienda.pedidos.aplicacion.puerto.entrada;

import com.javacadabra.tienda.pedidos.aplicacion.dto.entrada.CrearPedidoDTO;
import com.javacadabra.tienda.pedidos.aplicacion.dto.salida.PedidoDTO;
import org.jmolecules.architecture.hexagonal.PrimaryPort;

@PrimaryPort
public interface CrearPedidoPuertoEntrada {

	PedidoDTO crear(CrearPedidoDTO dto);
}
