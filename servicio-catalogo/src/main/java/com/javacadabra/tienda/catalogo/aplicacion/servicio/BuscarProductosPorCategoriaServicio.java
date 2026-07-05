package com.javacadabra.tienda.catalogo.aplicacion.servicio;

import com.javacadabra.tienda.catalogo.aplicacion.dto.salida.ProductoDTO;
import com.javacadabra.tienda.catalogo.aplicacion.mapper.ProductoMapper;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada.BuscarProductosPorCategoriaPuertoEntrada;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.salida.ProductoRepositorioPuertoSalida;
import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.CategoriaId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BuscarProductosPorCategoriaServicio implements BuscarProductosPorCategoriaPuertoEntrada {

	private final ProductoRepositorioPuertoSalida productoRepositorioPuertoSalida;
	private final ProductoMapper productoMapper;

	@Override
	public List<ProductoDTO> buscarPorCategoria(String categoriaId) {
		return productoRepositorioPuertoSalida.buscarPorCategoria(CategoriaId.de(categoriaId)).stream()
				.map(productoMapper::aDTO)
				.toList();
	}
}
