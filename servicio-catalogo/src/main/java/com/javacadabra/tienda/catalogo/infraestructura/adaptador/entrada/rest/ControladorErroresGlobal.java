package com.javacadabra.tienda.catalogo.infraestructura.adaptador.entrada.rest;

import com.javacadabra.tienda.catalogo.dominio.excepcion.CategoriaNoEncontradaException;
import com.javacadabra.tienda.catalogo.dominio.excepcion.ProductoNoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
public class ControladorErroresGlobal {

	private static final URI TIPO_PRODUCTO_NO_ENCONTRADO = URI.create("https://tienda.javacadabra.com/problemas/producto-no-encontrado");
	private static final URI TIPO_CATEGORIA_NO_ENCONTRADA = URI.create("https://tienda.javacadabra.com/problemas/categoria-no-encontrada");
	private static final URI TIPO_ARGUMENTO_INVALIDO = URI.create("https://tienda.javacadabra.com/problemas/argumento-invalido");

	@ExceptionHandler(ProductoNoEncontradoException.class)
	public ProblemDetail manejarProductoNoEncontrado(ProductoNoEncontradoException excepcion) {
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, excepcion.getMessage());
		problema.setType(TIPO_PRODUCTO_NO_ENCONTRADO);
		problema.setTitle("Producto no encontrado");
		problema.setProperty("productoId", excepcion.getId());
		return problema;
	}

	@ExceptionHandler(CategoriaNoEncontradaException.class)
	public ProblemDetail manejarCategoriaNoEncontrada(CategoriaNoEncontradaException excepcion) {
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, excepcion.getMessage());
		problema.setType(TIPO_CATEGORIA_NO_ENCONTRADA);
		problema.setTitle("Categoría no encontrada");
		problema.setProperty("categoriaId", excepcion.getId());
		return problema;
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ProblemDetail manejarArgumentoInvalido(IllegalArgumentException excepcion) {
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, excepcion.getMessage());
		problema.setType(TIPO_ARGUMENTO_INVALIDO);
		problema.setTitle("Argumento inválido");
		return problema;
	}
}
