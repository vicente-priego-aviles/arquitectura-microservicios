package com.javacadabra.tienda.catalogo.infraestructura.adaptador.entrada.mensajeria;

import com.javacadabra.tienda.catalogo.dominio.evento.ProductoCreadoEvento;
import com.javacadabra.tienda.catalogo.dominio.modelo.objetovalor.ProductoId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("rabbit")
@Testcontainers
@ExtendWith(OutputCaptureExtension.class)
class ProductoCreadoMensajeriaIntegrationTest {

	@Container
	@ServiceConnection
	static final RabbitMQContainer rabbitmq = new RabbitMQContainer(DockerImageName.parse("rabbitmq:4-management"));

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Test
	void productoCreadoViajaPorRabbitMqHastaElConsumidorReal(CapturedOutput output) {
		ProductoId productoId = ProductoId.generar();

		applicationEventPublisher.publishEvent(new ProductoCreadoEvento(productoId, Instant.now()));

		await().atMost(Duration.ofSeconds(10))
				.untilAsserted(() -> assertThat(output)
						.contains("Producto creado (vía mensajería asíncrona): " + productoId.valor()));
	}
}
