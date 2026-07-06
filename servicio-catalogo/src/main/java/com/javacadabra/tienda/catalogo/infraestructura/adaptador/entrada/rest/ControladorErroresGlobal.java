package com.javacadabra.tienda.catalogo.infraestructura.adaptador.entrada.rest;

import com.javacadabra.tienda.catalogo.dominio.excepcion.ProductoNoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControladorErroresGlobal {

	@ExceptionHandler(ProductoNoEncontradoException.class)
	public ResponseEntity<String> manejarProductoNoEncontrado(ProductoNoEncontradoException excepcion) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(excepcion.getMessage());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<String> manejarArgumentoInvalido(IllegalArgumentException excepcion) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(excepcion.getMessage());
	}
}
