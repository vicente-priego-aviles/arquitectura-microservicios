package com.javacadabra.tienda.catalogo.infraestructura.adaptador.entrada.rest;

import com.javacadabra.tienda.catalogo.dominio.excepcion.CategoriaNoEncontradaExcepcion;
import com.javacadabra.tienda.catalogo.dominio.excepcion.ProductoNoEncontradoExcepcion;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControladorErroresGlobal {

	@ExceptionHandler(ProductoNoEncontradoExcepcion.class)
	public ResponseEntity<String> manejarProductoNoEncontrado(ProductoNoEncontradoExcepcion excepcion) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(excepcion.getMessage());
	}

	@ExceptionHandler(CategoriaNoEncontradaExcepcion.class)
	public ResponseEntity<String> manejarCategoriaNoEncontrada(CategoriaNoEncontradaExcepcion excepcion) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(excepcion.getMessage());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<String> manejarArgumentoInvalido(IllegalArgumentException excepcion) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(excepcion.getMessage());
	}
}
