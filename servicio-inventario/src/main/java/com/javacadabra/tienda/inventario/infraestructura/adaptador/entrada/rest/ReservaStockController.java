package com.javacadabra.tienda.inventario.infraestructura.adaptador.entrada.rest;

import com.javacadabra.tienda.inventario.aplicacion.dto.entrada.ReservaStockPeticionDTO;
import com.javacadabra.tienda.inventario.aplicacion.puerto.entrada.ReservarStockPuertoEntrada;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.PrimaryAdapter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@PrimaryAdapter
@RestController
@RequestMapping("/api/v1/reservas-stock")
@RequiredArgsConstructor
public class ReservaStockController {

	private final ReservarStockPuertoEntrada reservarStockPuertoEntrada;

	@PostMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void reservar(@RequestBody ReservaStockPeticionDTO peticion) {
		reservarStockPuertoEntrada.reservar(peticion.pedidoId(), peticion.lineas());
	}
}
