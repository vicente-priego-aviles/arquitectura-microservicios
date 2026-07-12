package com.javacadabra.tienda.catalogo.infraestructura.adaptador.entrada.rest;

import com.javacadabra.tienda.catalogo.aplicacion.dto.entrada.RecomendarProductoDTO;
import com.javacadabra.tienda.catalogo.aplicacion.dto.salida.ProductoDTO;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada.BuscarProductoPuertoEntrada;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada.BuscarProductosPorCategoriaPuertoEntrada;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada.BuscarProductosRecomendadosPuertoEntrada;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada.CrearProductoPuertoEntrada;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada.RecomendarProductoPuertoEntrada;
import com.javacadabra.tienda.catalogo.dominio.excepcion.ProductoNoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(ProductoController.class)
@AutoConfigureRestTestClient
class ProductoControllerTest {

	@Autowired
	private MockMvcTester mvc;

	@Autowired
	private RestTestClient restTestClient;

	@MockitoBean
	private CrearProductoPuertoEntrada crearProductoPuertoEntrada;

	@MockitoBean
	private BuscarProductoPuertoEntrada buscarProductoPuertoEntrada;

	@MockitoBean
	private BuscarProductosPorCategoriaPuertoEntrada buscarProductosPorCategoriaPuertoEntrada;

	@MockitoBean
	private RecomendarProductoPuertoEntrada recomendarProductoPuertoEntrada;

	@MockitoBean
	private BuscarProductosRecomendadosPuertoEntrada buscarProductosRecomendadosPuertoEntrada;

	@Test
	void crearUnProductoDevuelve201YElCuerpoConMockMvcTester() {
		String categoriaId = UUID.randomUUID().toString();
		ProductoDTO creado = new ProductoDTO(UUID.randomUUID().toString(), "Camiseta", "Ropa de algodón", new BigDecimal("19.99"), categoriaId);
		when(crearProductoPuertoEntrada.crear(any())).thenReturn(creado);

		assertThat(mvc.post().uri("/api/productos")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"nombre":"Camiseta","descripcion":"Ropa de algodón","precio":19.99,"categoriaId":"%s"}
						""".formatted(categoriaId)))
				.hasStatus(HttpStatus.CREATED)
				.bodyJson()
				.extractingPath("$.nombre").isEqualTo("Camiseta");
	}

	@Test
	void buscarUnProductoExistenteDevuelveSuCuerpoConMockMvcTester() {
		String id = UUID.randomUUID().toString();
		ProductoDTO producto = new ProductoDTO(id, "Teclado", "Switches azules", new BigDecimal("49.99"), UUID.randomUUID().toString());
		when(buscarProductoPuertoEntrada.buscarPorId(id)).thenReturn(producto);

		assertThat(mvc.get().uri("/api/productos/{id}", id))
				.hasStatusOk()
				.bodyJson()
				.extractingPath("$.nombre").isEqualTo("Teclado");
	}

	@Test
	void buscarUnProductoExistenteDevuelveSuCuerpoTipadoConRestTestClient() {
		String id = UUID.randomUUID().toString();
		ProductoDTO producto = new ProductoDTO(id, "Teclado", "Switches azules", new BigDecimal("49.99"), UUID.randomUUID().toString());
		when(buscarProductoPuertoEntrada.buscarPorId(id)).thenReturn(producto);

		restTestClient.get().uri("/api/productos/{id}", id)
				.exchange()
				.expectStatus().isOk()
				.expectBody(ProductoDTO.class)
				.value(cuerpo -> assertThat(cuerpo.nombre()).isEqualTo("Teclado"));
	}

	@Test
	void buscarUnProductoInexistenteDevuelve404ConProblemDetailConMockMvcTester() {
		String id = UUID.randomUUID().toString();
		when(buscarProductoPuertoEntrada.buscarPorId(id)).thenThrow(new ProductoNoEncontradoException(id));

		assertThat(mvc.get().uri("/api/productos/{id}", id))
				.hasStatus(HttpStatus.NOT_FOUND)
				.bodyJson()
				.extractingPath("$.title").isEqualTo("Producto no encontrado");
	}

	@Test
	void buscarProductosPorCategoriaDevuelveLaListaTipadaConRestTestClient() {
		String categoriaId = UUID.randomUUID().toString();
		ProductoDTO producto = new ProductoDTO(UUID.randomUUID().toString(), "Camiseta", "Ropa de algodón", new BigDecimal("19.99"), categoriaId);
		when(buscarProductosPorCategoriaPuertoEntrada.buscarPorCategoria(categoriaId)).thenReturn(List.of(producto));

		restTestClient.get().uri("/api/productos?categoriaId={categoriaId}", categoriaId)
				.exchange()
				.expectStatus().isOk()
				.expectBody(new ParameterizedTypeReference<List<ProductoDTO>>() {
				})
				.value(cuerpo -> assertThat(cuerpo).extracting(ProductoDTO::nombre).containsExactly("Camiseta"));
	}

	@Test
	void recomendarUnProductoDevuelve204ConMockMvcTester() {
		String id = UUID.randomUUID().toString();
		String recomendadoId = UUID.randomUUID().toString();

		assertThat(mvc.post().uri("/api/productos/{id}/recomendaciones", id)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"productoRecomendadoId":"%s"}
						""".formatted(recomendadoId)))
				.hasStatus(HttpStatus.NO_CONTENT);

		verify(recomendarProductoPuertoEntrada).recomendar(id, new RecomendarProductoDTO(recomendadoId));
	}

	@Test
	void buscarRecomendadosDevuelveElArrayTipadoConRestTestClient() {
		String id = UUID.randomUUID().toString();
		ProductoDTO recomendado = new ProductoDTO(UUID.randomUUID().toString(), "Ratón", "Inalámbrico", new BigDecimal("29.99"), UUID.randomUUID().toString());
		when(buscarProductosRecomendadosPuertoEntrada.buscarRecomendados(id)).thenReturn(List.of(recomendado));

		restTestClient.get().uri("/api/productos/{id}/recomendaciones", id)
				.exchange()
				.expectStatus().isOk()
				.expectBody(ProductoDTO[].class)
				.value(cuerpo -> assertThat(cuerpo).hasSize(1));
	}
}