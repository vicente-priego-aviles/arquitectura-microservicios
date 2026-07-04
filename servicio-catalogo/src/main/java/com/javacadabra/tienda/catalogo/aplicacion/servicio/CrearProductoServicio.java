package com.javacadabra.tienda.catalogo.aplicacion.servicio;

import com.javacadabra.tienda.catalogo.aplicacion.dto.entrada.CrearProductoDTO;
import com.javacadabra.tienda.catalogo.aplicacion.dto.salida.ProductoDTO;
import com.javacadabra.tienda.catalogo.aplicacion.mapper.ProductoMapper;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada.CrearProductoPuertoEntrada;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.salida.ProductoRepositorioPuertoSalida;
import com.javacadabra.tienda.catalogo.dominio.modelo.agregado.Producto;
import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.Precio;
import org.springframework.stereotype.Service;

@Service
public class CrearProductoServicio implements CrearProductoPuertoEntrada {

	private final ProductoRepositorioPuertoSalida productoRepositorioPuertoSalida;
	private final ProductoMapper productoMapper;

	public CrearProductoServicio(ProductoRepositorioPuertoSalida productoRepositorioPuertoSalida, ProductoMapper productoMapper) {
		this.productoRepositorioPuertoSalida = productoRepositorioPuertoSalida;
		this.productoMapper = productoMapper;
	}

	@Override
	public ProductoDTO crear(CrearProductoDTO dto) {
		Producto producto = Producto.crear(dto.nombre(), dto.descripcion(), Precio.de(dto.precio()));
		Producto guardado = productoRepositorioPuertoSalida.guardar(producto);
		return productoMapper.aDTO(guardado);
	}
}
