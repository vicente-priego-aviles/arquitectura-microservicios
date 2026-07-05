package com.javacadabra.tienda.catalogo.aplicacion.servicio;

import com.javacadabra.tienda.catalogo.aplicacion.dto.entrada.RecomendarProductoDTO;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada.RecomendarProductoPuertoEntrada;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.salida.ProductoRepositorioPuertoSalida;
import com.javacadabra.tienda.catalogo.dominio.excepcion.ProductoNoEncontradoExcepcion;
import com.javacadabra.tienda.catalogo.dominio.modelo.agregado.Producto;
import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.ProductoId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecomendarProductoServicio implements RecomendarProductoPuertoEntrada {

	private final ProductoRepositorioPuertoSalida productoRepositorioPuertoSalida;

	@Override
	public void recomendar(String productoId, RecomendarProductoDTO dto) {
		ProductoId id = ProductoId.de(productoId);
		ProductoId recomendadoId = ProductoId.de(dto.productoRecomendadoId());

		Producto producto = productoRepositorioPuertoSalida.buscarPorId(id)
				.orElseThrow(() -> new ProductoNoEncontradoExcepcion(productoId));
		productoRepositorioPuertoSalida.buscarPorId(recomendadoId)
				.orElseThrow(() -> new ProductoNoEncontradoExcepcion(dto.productoRecomendadoId()));

		producto.validarRecomendacion(recomendadoId);
		productoRepositorioPuertoSalida.agregarRecomendacion(id, recomendadoId);
	}
}
