package com.javacadabra.tienda.inventario.infraestructura.adaptador.entrada.rest;

import com.javacadabra.tienda.inventario.dominio.excepcion.StockInsuficienteException;
import com.javacadabra.tienda.inventario.dominio.excepcion.StockNoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
public class ControladorErroresGlobal {

	private static final URI TIPO_STOCK_INSUFICIENTE = URI.create("https://tienda.javacadabra.com/problemas/stock-insuficiente");
	private static final URI TIPO_STOCK_NO_ENCONTRADO = URI.create("https://tienda.javacadabra.com/problemas/stock-no-encontrado");

	@ExceptionHandler(StockInsuficienteException.class)
	public ProblemDetail manejarStockInsuficiente(StockInsuficienteException excepcion) {
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, excepcion.getMessage());
		problema.setType(TIPO_STOCK_INSUFICIENTE);
		problema.setTitle("Stock insuficiente");
		problema.setProperty("productoId", excepcion.getProductoId().valor());
		return problema;
	}

	@ExceptionHandler(StockNoEncontradoException.class)
	public ProblemDetail manejarStockNoEncontrado(StockNoEncontradoException excepcion) {
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, excepcion.getMessage());
		problema.setType(TIPO_STOCK_NO_ENCONTRADO);
		problema.setTitle("Stock no encontrado");
		problema.setProperty("productoId", excepcion.getProductoId().valor());
		return problema;
	}
}
