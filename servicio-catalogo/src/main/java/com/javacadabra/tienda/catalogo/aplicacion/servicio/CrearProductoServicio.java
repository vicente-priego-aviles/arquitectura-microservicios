package com.javacadabra.tienda.catalogo.aplicacion.servicio;

import com.javacadabra.tienda.catalogo.aplicacion.dto.entrada.CrearProductoDTO;
import com.javacadabra.tienda.catalogo.aplicacion.dto.salida.ProductoDTO;
import com.javacadabra.tienda.catalogo.aplicacion.mapper.ProductoMapper;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada.CrearProductoPuertoEntrada;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.salida.CategoriaRepositorioPuertoSalida;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.salida.ProductoRepositorioPuertoSalida;
import com.javacadabra.tienda.catalogo.dominio.excepcion.CategoriaNoEncontradaException;
import com.javacadabra.tienda.catalogo.dominio.modelo.agregado.Producto;
import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.CategoriaId;
import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.Precio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CrearProductoServicio implements CrearProductoPuertoEntrada {

	private final ProductoRepositorioPuertoSalida productoRepositorioPuertoSalida;
	private final CategoriaRepositorioPuertoSalida categoriaRepositorioPuertoSalida;
	private final ProductoMapper productoMapper;

	@Override
	public ProductoDTO crear(CrearProductoDTO dto) {
		CategoriaId categoriaId = CategoriaId.de(dto.categoriaId());
		categoriaRepositorioPuertoSalida.buscarPorId(categoriaId)
				.orElseThrow(() -> new CategoriaNoEncontradaException(dto.categoriaId()));

		Producto producto = Producto.crear(dto.nombre(), dto.descripcion(), Precio.de(dto.precio()), categoriaId);
		Producto guardado = productoRepositorioPuertoSalida.guardar(producto);
		return productoMapper.aDTO(guardado);
	}
}
