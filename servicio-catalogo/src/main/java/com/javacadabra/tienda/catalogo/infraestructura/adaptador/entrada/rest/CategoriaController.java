package com.javacadabra.tienda.catalogo.infraestructura.adaptador.entrada.rest;

import com.javacadabra.tienda.catalogo.aplicacion.dto.entrada.CrearCategoriaDTO;
import com.javacadabra.tienda.catalogo.aplicacion.dto.salida.CategoriaDTO;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada.BuscarCategoriaPuertoEntrada;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada.CrearCategoriaPuertoEntrada;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

	private final CrearCategoriaPuertoEntrada crearCategoriaPuertoEntrada;
	private final BuscarCategoriaPuertoEntrada buscarCategoriaPuertoEntrada;

	@PostMapping
	public ResponseEntity<CategoriaDTO> crear(@RequestBody CrearCategoriaDTO dto) {
		CategoriaDTO creada = crearCategoriaPuertoEntrada.crear(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(creada);
	}

	@GetMapping("/{id}")
	public ResponseEntity<CategoriaDTO> buscarPorId(@PathVariable String id) {
		return ResponseEntity.ok(buscarCategoriaPuertoEntrada.buscarPorId(id));
	}
}
