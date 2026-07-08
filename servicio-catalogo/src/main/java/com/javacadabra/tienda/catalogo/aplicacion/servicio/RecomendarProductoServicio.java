package com.javacadabra.tienda.catalogo.aplicacion.servicio;

import com.javacadabra.tienda.catalogo.aplicacion.dto.entrada.RecomendarProductoDTO;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada.RecomendarProductoPuertoEntrada;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.salida.ProductoRepositorioPuertoSalida;
import com.javacadabra.tienda.catalogo.dominio.evento.RecomendacionAñadidaEvento;
import com.javacadabra.tienda.catalogo.dominio.excepcion.ProductoNoEncontradoException;
import com.javacadabra.tienda.catalogo.dominio.modelo.agregado.Producto;
import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.ProductoId;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RecomendarProductoServicio implements RecomendarProductoPuertoEntrada {

	private final ProductoRepositorioPuertoSalida productoRepositorioPuertoSalida;
	private final ApplicationEventPublisher applicationEventPublisher;

	@Override
	@Transactional
	public void recomendar(String productoId, RecomendarProductoDTO dto) {
		ProductoId id = ProductoId.de(productoId);
		ProductoId recomendadoId = ProductoId.de(dto.productoRecomendadoId());

		Producto producto = productoRepositorioPuertoSalida.buscarPorId(id)
				.orElseThrow(() -> new ProductoNoEncontradoException(productoId));
		productoRepositorioPuertoSalida.buscarPorId(recomendadoId)
				.orElseThrow(() -> new ProductoNoEncontradoException(dto.productoRecomendadoId()));

		producto.validarRecomendacion(recomendadoId);
		productoRepositorioPuertoSalida.agregarRecomendacion(id, recomendadoId);
		applicationEventPublisher.publishEvent(new RecomendacionAñadidaEvento(id, recomendadoId, Instant.now()));
	}
}
