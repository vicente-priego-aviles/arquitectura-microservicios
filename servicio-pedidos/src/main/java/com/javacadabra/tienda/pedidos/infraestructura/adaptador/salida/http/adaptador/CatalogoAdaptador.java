package com.javacadabra.tienda.pedidos.infraestructura.adaptador.salida.http.adaptador;

import com.javacadabra.tienda.pedidos.aplicacion.puerto.salida.CatalogoPuertoSalida;
import com.javacadabra.tienda.pedidos.aplicacion.puerto.salida.ProductoCatalogoDTO;
import com.javacadabra.tienda.pedidos.dominio.excepcion.ProductoInexistenteException;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.ProductoId;
import com.javacadabra.tienda.pedidos.infraestructura.adaptador.salida.http.cliente.CatalogoHttpExchange;
import com.javacadabra.tienda.pedidos.infraestructura.adaptador.salida.http.cliente.ProductoCatalogoRespuesta;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

@Component
@RequiredArgsConstructor
public class CatalogoAdaptador implements CatalogoPuertoSalida {

	private final CatalogoHttpExchange catalogoHttpExchange;

	@Override
	public ProductoCatalogoDTO buscarProductoPorId(ProductoId productoId) {
		try {
			ProductoCatalogoRespuesta respuesta = catalogoHttpExchange.buscarProductoPorId(productoId.valor());
			return new ProductoCatalogoDTO(respuesta.id(), respuesta.nombre(), respuesta.precio());
		} catch (HttpClientErrorException.NotFound excepcion) {
			throw new ProductoInexistenteException(productoId.valor());
		}
	}
}
