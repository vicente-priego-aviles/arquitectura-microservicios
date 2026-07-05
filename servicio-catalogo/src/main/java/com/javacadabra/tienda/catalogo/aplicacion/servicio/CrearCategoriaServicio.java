package com.javacadabra.tienda.catalogo.aplicacion.servicio;

import com.javacadabra.tienda.catalogo.aplicacion.dto.entrada.CrearCategoriaDTO;
import com.javacadabra.tienda.catalogo.aplicacion.dto.salida.CategoriaDTO;
import com.javacadabra.tienda.catalogo.aplicacion.mapper.CategoriaMapper;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada.CrearCategoriaPuertoEntrada;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.salida.CategoriaRepositorioPuertoSalida;
import com.javacadabra.tienda.catalogo.dominio.modelo.agregado.Categoria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CrearCategoriaServicio implements CrearCategoriaPuertoEntrada {

	private final CategoriaRepositorioPuertoSalida categoriaRepositorioPuertoSalida;
	private final CategoriaMapper categoriaMapper;

	@Override
	public CategoriaDTO crear(CrearCategoriaDTO dto) {
		Categoria categoria = Categoria.crear(dto.nombre());
		Categoria guardada = categoriaRepositorioPuertoSalida.guardar(categoria);
		return categoriaMapper.aDTO(guardada);
	}
}
