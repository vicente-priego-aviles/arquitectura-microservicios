package com.javacadabra.tienda.pedidos.infraestructura.adaptador.entrada.rest;

import com.javacadabra.tienda.pedidos.dominio.excepcion.CatalogoNoDisponibleException;
import com.javacadabra.tienda.pedidos.dominio.excepcion.ProductoInexistenteException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.resilience.InvocationRejectedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;

import java.net.URI;

@RestControllerAdvice
public class ControladorErroresGlobal {

	private static final URI TIPO_PRODUCTO_INEXISTENTE = URI.create("https://tienda.javacadabra.com/problemas/producto-inexistente");
	private static final URI TIPO_ARGUMENTO_INVALIDO = URI.create("https://tienda.javacadabra.com/problemas/argumento-invalido");
	private static final URI TIPO_CATALOGO_SATURADO = URI.create("https://tienda.javacadabra.com/problemas/catalogo-saturado");
	private static final URI TIPO_CATALOGO_NO_DISPONIBLE = URI.create("https://tienda.javacadabra.com/problemas/catalogo-no-disponible");
	private static final URI TIPO_SERVICIO_EXTERNO_NO_DISPONIBLE = URI.create("https://tienda.javacadabra.com/problemas/servicio-externo-no-disponible");

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

	@ExceptionHandler(InvocationRejectedException.class)
	public ProblemDetail manejarCatalogoSaturado(InvocationRejectedException excepcion) {
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
				"El catálogo tiene demasiadas peticiones en curso ahora mismo; inténtalo de nuevo en unos segundos");
		problema.setType(TIPO_CATALOGO_SATURADO);
		problema.setTitle("Catálogo saturado");
		return problema;
	}

	@ExceptionHandler(CatalogoNoDisponibleException.class)
	public ProblemDetail manejarCatalogoNoDisponible(CatalogoNoDisponibleException excepcion) {
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, excepcion.getMessage());
		problema.setType(TIPO_CATALOGO_NO_DISPONIBLE);
		problema.setTitle("Catálogo no disponible");
		problema.setProperty("productoId", excepcion.getProductoId());
		return problema;
	}

	// Compartido entre catálogo (con reintentos, capítulo 8) e inventario (sin reintentos, capítulo 14):
	// Spring lanza este mismo tipo ante cualquier fallo de E/S, sin distinguir a qué servicio pertenecía
	// la llamada — de ahí que el mensaje ya no pueda nombrar a uno de los dos en concreto.
	@ExceptionHandler(ResourceAccessException.class)
	public ProblemDetail manejarServicioExternoNoDisponible(ResourceAccessException excepcion) {
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
				"Un servicio del que depende esta petición no responde");
		problema.setType(TIPO_SERVICIO_EXTERNO_NO_DISPONIBLE);
		problema.setTitle("Servicio externo no disponible");
		return problema;
	}
}
