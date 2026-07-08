package com.javacadabra.tienda.catalogo.aplicacion.servicio;

import com.javacadabra.tienda.catalogo.aplicacion.dto.entrada.CrearProductoDTO;
import com.javacadabra.tienda.catalogo.aplicacion.mapper.ProductoMapper;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.salida.CategoriaRepositorioPuertoSalida;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.salida.ProductoRepositorioPuertoSalida;
import com.javacadabra.tienda.catalogo.dominio.evento.ProductoCreadoEvento;
import com.javacadabra.tienda.catalogo.dominio.excepcion.CategoriaNoEncontradaException;
import com.javacadabra.tienda.catalogo.dominio.modelo.agregado.Categoria;
import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.CategoriaId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrearProductoServicioTest {

	@Mock
	private ProductoRepositorioPuertoSalida productoRepositorioPuertoSalida;

	@Mock
	private CategoriaRepositorioPuertoSalida categoriaRepositorioPuertoSalida;

	@Mock
	private ApplicationEventPublisher applicationEventPublisher;

	private final ProductoMapper productoMapper = new ProductoMapper() {
	};

	@Test
	void crearUnProductoConUnaCategoriaInexistenteLanzaExcepcionYNoGuardaNada() {
		CategoriaId categoriaId = CategoriaId.generar();
		when(categoriaRepositorioPuertoSalida.buscarPorId(categoriaId)).thenReturn(Optional.empty());

		CrearProductoServicio servicio = new CrearProductoServicio(productoRepositorioPuertoSalida, categoriaRepositorioPuertoSalida, productoMapper, applicationEventPublisher);
		CrearProductoDTO dto = new CrearProductoDTO("Camiseta", "100% algodón", BigDecimal.TEN, categoriaId.valor());

		assertThatThrownBy(() -> servicio.crear(dto)).isInstanceOf(CategoriaNoEncontradaException.class);

		verify(productoRepositorioPuertoSalida, never()).guardar(any());
		verify(applicationEventPublisher, never()).publishEvent(any());
	}

	@Test
	void crearUnProductoConUnaCategoriaExistenteLoGuardaYPublicaElEvento() {
		CategoriaId categoriaId = CategoriaId.generar();
		Categoria categoria = Categoria.reconstruir(categoriaId, "Ropa");
		when(categoriaRepositorioPuertoSalida.buscarPorId(categoriaId)).thenReturn(Optional.of(categoria));
		when(productoRepositorioPuertoSalida.guardar(any())).thenAnswer(invocacion -> invocacion.getArgument(0));

		CrearProductoServicio servicio = new CrearProductoServicio(productoRepositorioPuertoSalida, categoriaRepositorioPuertoSalida, productoMapper, applicationEventPublisher);
		CrearProductoDTO dto = new CrearProductoDTO("Camiseta", "100% algodón", BigDecimal.TEN, categoriaId.valor());

		var productoCreado = servicio.crear(dto);

		verify(productoRepositorioPuertoSalida).guardar(any());
		assertThat(productoCreado.categoriaId()).isEqualTo(categoriaId.valor());

		var capturador = ArgumentCaptor.forClass(ProductoCreadoEvento.class);
		verify(applicationEventPublisher).publishEvent(capturador.capture());
		assertThat(capturador.getValue().productoId().valor()).isEqualTo(productoCreado.id());
	}
}
