package com.javacadabra.tienda.pedidos.infraestructura.adaptador.entrada.rest;

import com.javacadabra.tienda.pedidos.dominio.excepcion.ProductoInexistenteException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
public class ControladorErroresGlobal {

	private static final URI TIPO_PRODUCTO_INEXISTENTE = URI.create("https://tienda.javacadabra.com/problemas/producto-inexistente");
	private static final URI TIPO_ARGUMENTO_INVALIDO = URI.create("https://tienda.javacadabra.com/problemas/argumento-invalido");

	@ExceptionHandler(ProductoInexistenteException.class)
	public ProblemDetail manejarProductoInexistente(ProductoInexistenteException excepcion) {
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, excepcion.getMessage());
		problema.setType(TIPO_PRODUCTO_INEXISTENTE);
		problema.setTitle("Producto inexistente");
		problema.setProperty("productoId", excepcion.getId());
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
