package com.javacadabra.tienda.catalogo.infraestructura.adaptador.entrada.rest;

import com.javacadabra.tienda.catalogo.aplicacion.dto.entrada.CrearProductoDTO;
import com.javacadabra.tienda.catalogo.aplicacion.dto.salida.ProductoDTO;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada.BuscarProductoPuertoEntrada;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada.CrearProductoPuertoEntrada;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

	private final CrearProductoPuertoEntrada crearProductoPuertoEntrada;
	private final BuscarProductoPuertoEntrada buscarProductoPuertoEntrada;

	public ProductoController(CrearProductoPuertoEntrada crearProductoPuertoEntrada, BuscarProductoPuertoEntrada buscarProductoPuertoEntrada) {
		this.crearProductoPuertoEntrada = crearProductoPuertoEntrada;
		this.buscarProductoPuertoEntrada = buscarProductoPuertoEntrada;
	}

	@PostMapping
	public ResponseEntity<ProductoDTO> crear(@RequestBody CrearProductoDTO dto) {
		ProductoDTO creado = crearProductoPuertoEntrada.crear(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(creado);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ProductoDTO> buscarPorId(@PathVariable String id) {
		return ResponseEntity.ok(buscarProductoPuertoEntrada.buscarPorId(id));
	}
}
