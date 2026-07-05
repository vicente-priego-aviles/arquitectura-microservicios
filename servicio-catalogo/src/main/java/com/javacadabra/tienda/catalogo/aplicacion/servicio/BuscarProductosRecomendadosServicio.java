package com.javacadabra.tienda.catalogo.aplicacion.servicio;

import com.javacadabra.tienda.catalogo.aplicacion.dto.salida.ProductoDTO;
import com.javacadabra.tienda.catalogo.aplicacion.mapper.ProductoMapper;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada.BuscarProductosRecomendadosPuertoEntrada;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.salida.ProductoRepositorioPuertoSalida;
import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.ProductoId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BuscarProductosRecomendadosServicio implements BuscarProductosRecomendadosPuertoEntrada {

	private final ProductoRepositorioPuertoSalida productoRepositorioPuertoSalida;
	private final ProductoMapper productoMapper;

	@Override
	public List<ProductoDTO> buscarRecomendados(String productoId) {
		return productoRepositorioPuertoSalida.buscarRecomendados(ProductoId.de(productoId)).stream()
				.map(productoMapper::aDTO)
				.toList();
	}
}
