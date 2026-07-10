package com.javacadabra.tienda.catalogo.infraestructura.adaptador.entrada.rest;

import com.javacadabra.tienda.catalogo.aplicacion.dto.entrada.CrearCategoriaDTO;
import com.javacadabra.tienda.catalogo.aplicacion.dto.salida.CategoriaDTO;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada.BuscarCategoriaPuertoEntrada;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada.CrearCategoriaPuertoEntrada;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CategoriaController implements CategoriaControllerApi {

	private final CrearCategoriaPuertoEntrada crearCategoriaPuertoEntrada;
	private final BuscarCategoriaPuertoEntrada buscarCategoriaPuertoEntrada;

	@Override
	public ResponseEntity<CategoriaDTO> crear(CrearCategoriaDTO dto) {
		CategoriaDTO creada = crearCategoriaPuertoEntrada.crear(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(creada);
	}

	@Override
	public ResponseEntity<CategoriaDTO> buscarPorId(String id) {
		return ResponseEntity.ok(buscarCategoriaPuertoEntrada.buscarPorId(id));
	}
}
