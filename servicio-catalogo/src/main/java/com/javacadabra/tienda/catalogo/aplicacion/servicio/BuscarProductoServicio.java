package com.javacadabra.tienda.catalogo.aplicacion.servicio;

import com.javacadabra.tienda.catalogo.aplicacion.dto.salida.ProductoDTO;
import com.javacadabra.tienda.catalogo.aplicacion.mapper.ProductoMapper;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada.BuscarProductoPuertoEntrada;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.salida.ProductoRepositorioPuertoSalida;
import com.javacadabra.tienda.catalogo.dominio.excepcion.ProductoNoEncontradoExcepcion;
import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.ProductoId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BuscarProductoServicio implements BuscarProductoPuertoEntrada {

	private final ProductoRepositorioPuertoSalida productoRepositorioPuertoSalida;
	private final ProductoMapper productoMapper;

	@Override
	public ProductoDTO buscarPorId(String id) {
		return productoRepositorioPuertoSalida.buscarPorId(ProductoId.de(id))
				.map(productoMapper::aDTO)
				.orElseThrow(() -> new ProductoNoEncontradoExcepcion(id));
	}
}
