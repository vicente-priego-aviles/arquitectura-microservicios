package com.javacadabra.tienda.catalogo.infraestructura.adaptador.entrada.rest;

import com.javacadabra.tienda.catalogo.aplicacion.dto.entrada.CrearCategoriaDTO;
import com.javacadabra.tienda.catalogo.aplicacion.dto.salida.CategoriaDTO;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada.BuscarCategoriaPuertoEntrada;
import com.javacadabra.tienda.catalogo.aplicacion.puerto.entrada.CrearCategoriaPuertoEntrada;
import com.javacadabra.tienda.catalogo.dominio.excepcion.CategoriaNoEncontradaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaControllerTest {

	@Mock
	private CrearCategoriaPuertoEntrada crearCategoriaPuertoEntrada;

	@Mock
	private BuscarCategoriaPuertoEntrada buscarCategoriaPuertoEntrada;

	private RestTestClient restTestClient;

	@BeforeEach
	void construirClienteSinContextoDeSpring() {
		CategoriaController controller = new CategoriaController(crearCategoriaPuertoEntrada, buscarCategoriaPuertoEntrada);
		restTestClient = RestTestClient.bindToController(controller)
				.configureServer(builder -> builder.setControllerAdvice(new ControladorErroresGlobal()))
				.build();
	}

	@Test
	void crearUnaCategoriaDevuelve201YElCuerpo() {
		CategoriaDTO creada = new CategoriaDTO(UUID.randomUUID().toString(), "Ropa");
		when(crearCategoriaPuertoEntrada.crear(any())).thenReturn(creada);

		restTestClient.post().uri("/api/categorias")
				.contentType(MediaType.APPLICATION_JSON)
				.body(new CrearCategoriaDTO("Ropa"))
				.exchange()
				.expectStatus().isCreated()
				.expectBody(CategoriaDTO.class)
				.value(cuerpo -> assertThat(cuerpo.nombre()).isEqualTo("Ropa"));
	}

	@Test
	void buscarUnaCategoriaExistenteDevuelveSuCuerpo() {
		String id = UUID.randomUUID().toString();
		CategoriaDTO categoria = new CategoriaDTO(id, "Electrónica");
		when(buscarCategoriaPuertoEntrada.buscarPorId(id)).thenReturn(categoria);

		restTestClient.get().uri("/api/categorias/{id}", id)
				.exchange()
				.expectStatus().isOk()
				.expectBody(CategoriaDTO.class)
				.value(cuerpo -> assertThat(cuerpo.nombre()).isEqualTo("Electrónica"));
	}

	@Test
	void buscarUnaCategoriaInexistenteDevuelve404ConProblemDetail() {
		String id = UUID.randomUUID().toString();
		when(buscarCategoriaPuertoEntrada.buscarPorId(id)).thenThrow(new CategoriaNoEncontradaException(id));

		restTestClient.get().uri("/api/categorias/{id}", id)
				.exchange()
				.expectStatus().isNotFound()
				.expectBody(ProblemDetail.class)
				.value(cuerpo -> assertThat(cuerpo.getTitle()).isEqualTo("Categoría no encontrada"));
	}
}