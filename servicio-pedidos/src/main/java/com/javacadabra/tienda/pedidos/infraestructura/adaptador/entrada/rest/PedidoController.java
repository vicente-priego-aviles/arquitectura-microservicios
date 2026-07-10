package com.javacadabra.tienda.pedidos.infraestructura.adaptador.entrada.rest;

import com.javacadabra.tienda.pedidos.aplicacion.dto.entrada.CrearPedidoDTO;
import com.javacadabra.tienda.pedidos.aplicacion.dto.salida.PedidoDTO;
import com.javacadabra.tienda.pedidos.aplicacion.puerto.entrada.CrearPedidoPuertoEntrada;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PedidoController implements PedidoControllerApi {

	private final CrearPedidoPuertoEntrada crearPedidoPuertoEntrada;

	@Override
	public ResponseEntity<PedidoDTO> crear(CrearPedidoDTO dto) {
		PedidoDTO creado = crearPedidoPuertoEntrada.crear(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(creado);
	}
}
