package com.javacadabra.tienda.catalogo.aplicacion.servicio;

import com.javacadabra.tienda.catalogo.aplicacion.dto.entrada.RecomendarProductoDTO;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.salida.ProductoRepositorioPuertoSalida;
import com.javacadabra.tienda.catalogo.dominio.excepcion.ProductoNoEncontradoException;
import com.javacadabra.tienda.catalogo.dominio.modelo.agregado.Producto;
import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.CategoriaId;
import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.Precio;
import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.ProductoId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecomendarProductoServicioTest {

	@Mock
	private ProductoRepositorioPuertoSalida productoRepositorioPuertoSalida;

	@Test
	void recomendarUnProductoInexistenteLanzaExcepcion() {
		ProductoId id = ProductoId.generar();
		when(productoRepositorioPuertoSalida.buscarPorId(id)).thenReturn(Optional.empty());

		RecomendarProductoServicio servicio = new RecomendarProductoServicio(productoRepositorioPuertoSalida);
		RecomendarProductoDTO dto = new RecomendarProductoDTO(ProductoId.generar().valor());

		assertThatThrownBy(() -> servicio.recomendar(id.valor(), dto)).isInstanceOf(ProductoNoEncontradoException.class);

		verify(productoRepositorioPuertoSalida, never()).agregarRecomendacion(any(), any());
	}

	@Test
	void recomendarUnProductoASiMismoLanzaExcepcion() {
		Producto producto = Producto.crear("Camiseta", "100% algodón", Precio.de(BigDecimal.TEN), CategoriaId.generar());
		when(productoRepositorioPuertoSalida.buscarPorId(producto.id())).thenReturn(Optional.of(producto));

		RecomendarProductoServicio servicio = new RecomendarProductoServicio(productoRepositorioPuertoSalida);
		RecomendarProductoDTO dto = new RecomendarProductoDTO(producto.id().valor());

		assertThatThrownBy(() -> servicio.recomendar(producto.id().valor(), dto)).isInstanceOf(IllegalArgumentException.class);

		verify(productoRepositorioPuertoSalida, never()).agregarRecomendacion(any(), any());
	}

	@Test
	void recomendarUnProductoDistintoExistenteAgregaLaRecomendacion() {
		Producto producto = Producto.crear("Camiseta", "100% algodón", Precio.de(BigDecimal.TEN), CategoriaId.generar());
		Producto recomendado = Producto.crear("Pantalón", "Vaquero", Precio.de(BigDecimal.TEN), CategoriaId.generar());
		when(productoRepositorioPuertoSalida.buscarPorId(producto.id())).thenReturn(Optional.of(producto));
		when(productoRepositorioPuertoSalida.buscarPorId(recomendado.id())).thenReturn(Optional.of(recomendado));

		RecomendarProductoServicio servicio = new RecomendarProductoServicio(productoRepositorioPuertoSalida);
		RecomendarProductoDTO dto = new RecomendarProductoDTO(recomendado.id().valor());

		servicio.recomendar(producto.id().valor(), dto);

		verify(productoRepositorioPuertoSalida).agregarRecomendacion(producto.id(), recomendado.id());
	}
}
