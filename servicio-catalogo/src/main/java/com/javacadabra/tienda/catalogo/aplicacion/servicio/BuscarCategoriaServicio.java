package com.javacadabra.tienda.catalogo.aplicacion.servicio;

import com.javacadabra.tienda.catalogo.aplicacion.dto.salida.CategoriaDTO;
import com.javacadabra.tienda.catalogo.aplicacion.mapper.CategoriaMapper;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada.BuscarCategoriaPuertoEntrada;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.salida.CategoriaRepositorioPuertoSalida;
import com.javacadabra.tienda.catalogo.dominio.excepcion.CategoriaNoEncontradaExcepcion;
import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.CategoriaId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BuscarCategoriaServicio implements BuscarCategoriaPuertoEntrada {

	private final CategoriaRepositorioPuertoSalida categoriaRepositorioPuertoSalida;
	private final CategoriaMapper categoriaMapper;

	@Override
	public CategoriaDTO buscarPorId(String id) {
		return categoriaRepositorioPuertoSalida.buscarPorId(CategoriaId.de(id))
				.map(categoriaMapper::aDTO)
				.orElseThrow(() -> new CategoriaNoEncontradaExcepcion(id));
	}
}
