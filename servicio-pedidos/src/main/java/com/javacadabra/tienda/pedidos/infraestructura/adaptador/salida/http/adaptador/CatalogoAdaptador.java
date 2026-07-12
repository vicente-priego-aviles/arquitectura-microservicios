package com.javacadabra.tienda.pedidos.infraestructura.adaptador.salida.http.adaptador;

import com.javacadabra.tienda.pedidos.aplicacion.puerto.salida.CatalogoPuertoSalida;
import com.javacadabra.tienda.pedidos.aplicacion.puerto.salida.ProductoCatalogoDTO;
import com.javacadabra.tienda.pedidos.dominio.excepcion.CatalogoNoDisponibleException;
import com.javacadabra.tienda.pedidos.dominio.excepcion.ProductoInexistenteException;
import com.javacadabra.tienda.pedidos.dominio.modelo.objetovalor.ProductoId;
import com.javacadabra.tienda.pedidos.infraestructura.adaptador.salida.http.cliente.CatalogoHttpExchange;
import com.javacadabra.tienda.pedidos.infraestructura.adaptador.salida.http.cliente.ProductoCatalogoRespuesta;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

@SecondaryAdapter
@Component
@RequiredArgsConstructor
public class CatalogoAdaptador implements CatalogoPuertoSalida {

	private final CatalogoHttpExchange catalogoHttpExchange;

	@CircuitBreaker(name = "catalogo", fallbackMethod = "catalogoNoDisponible")
	@ConcurrencyLimit(limit = 2, policy = ConcurrencyLimit.ThrottlePolicy.REJECT)
	@Retryable(
			includes = ResourceAccessException.class,
			maxRetries = 3,
			delay = 200,
			multiplier = 2,
			jitter = 50,
			maxDelay = 1500)
	@Override
	public ProductoCatalogoDTO buscarProductoPorId(ProductoId productoId) {
		try {
			ProductoCatalogoRespuesta respuesta = catalogoHttpExchange.buscarProductoPorId(productoId.valor());
			return new ProductoCatalogoDTO(respuesta.id(), respuesta.nombre(), respuesta.precio());
		} catch (HttpClientErrorException.NotFound excepcion) {
			throw new ProductoInexistenteException(productoId.valor());
		}
	}

	// Resilience4j invoca este método por reflexión a partir del nombre en fallbackMethod;
	// IntelliJ no lo sabe y marca el método y la excepción como no usados.
	@SuppressWarnings("unused")
	private ProductoCatalogoDTO catalogoNoDisponible(ProductoId productoId, CallNotPermittedException excepcion) {
		throw new CatalogoNoDisponibleException(productoId.valor());
	}
}
