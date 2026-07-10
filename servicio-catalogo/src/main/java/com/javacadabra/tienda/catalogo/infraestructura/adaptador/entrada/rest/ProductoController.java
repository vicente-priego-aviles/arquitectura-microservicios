package com.javacadabra.tienda.catalogo.infraestructura.adaptador.entrada.rest;

import com.javacadabra.tienda.catalogo.aplicacion.dto.entrada.CrearProductoDTO;
import com.javacadabra.tienda.catalogo.aplicacion.dto.entrada.RecomendarProductoDTO;
import com.javacadabra.tienda.catalogo.aplicacion.dto.salida.ProductoDTO;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada.BuscarProductoPuertoEntrada;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada.BuscarProductosPorCategoriaPuertoEntrada;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada.BuscarProductosRecomendadosPuertoEntrada;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada.CrearProductoPuertoEntrada;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada.RecomendarProductoPuertoEntrada;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductoController implements ProductoControllerApi {

	private final CrearProductoPuertoEntrada crearProductoPuertoEntrada;
	private final BuscarProductoPuertoEntrada buscarProductoPuertoEntrada;
	private final BuscarProductosPorCategoriaPuertoEntrada buscarProductosPorCategoriaPuertoEntrada;
	private final RecomendarProductoPuertoEntrada recomendarProductoPuertoEntrada;
	private final BuscarProductosRecomendadosPuertoEntrada buscarProductosRecomendadosPuertoEntrada;

	@Override
	public ResponseEntity<ProductoDTO> crear(CrearProductoDTO dto) {
		ProductoDTO creado = crearProductoPuertoEntrada.crear(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(creado);
	}

	@Override
	public ResponseEntity<ProductoDTO> buscarPorId(String id) {
		return ResponseEntity.ok(buscarProductoPuertoEntrada.buscarPorId(id));
	}

	@Override
	public ResponseEntity<List<ProductoDTO>> buscarPorCategoria(String categoriaId) {
		return ResponseEntity.ok(buscarProductosPorCategoriaPuertoEntrada.buscarPorCategoria(categoriaId));
	}

	@Override
	public ResponseEntity<Void> recomendar(String id, RecomendarProductoDTO dto) {
		recomendarProductoPuertoEntrada.recomendar(id, dto);
		return ResponseEntity.noContent().build();
	}

	@Override
	public ResponseEntity<List<ProductoDTO>> buscarRecomendados(String id) {
		return ResponseEntity.ok(buscarProductosRecomendadosPuertoEntrada.buscarRecomendados(id));
	}
}
