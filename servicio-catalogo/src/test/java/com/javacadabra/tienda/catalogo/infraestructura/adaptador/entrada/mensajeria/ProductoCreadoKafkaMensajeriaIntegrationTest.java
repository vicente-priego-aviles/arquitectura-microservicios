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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

// Prueba, con un broker real, que el mismo StreamBridge/Consumer<T> del capítulo 11
// (pensados y verificados sobre RabbitMQ) funcionan igual sobre Kafka sin ningún cambio
// de código — solo el perfil "kafka" (defaultBinder: kafka).
@SpringBootTest
@ActiveProfiles("kafka")
@Testcontainers
@ExtendWith(OutputCaptureExtension.class)
class ProductoCreadoKafkaMensajeriaIntegrationTest {

	@Container
	@ServiceConnection
	static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka:4.3.1"));

	// spring.cloud.stream.kafka.binder.brokers es una propiedad propia del binder de
	// Spring Cloud Stream: @ServiceConnection solo rellena spring.kafka.*, que el binder
	// no lee.
	@DynamicPropertySource
	static void streamBinderBrokers(DynamicPropertyRegistry registry) {
		registry.add("spring.cloud.stream.kafka.binder.brokers", kafka::getBootstrapServers);
	}

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Test
	void productoCreadoViajaPorKafkaHastaElConsumidorReal(CapturedOutput output) {
		ProductoId productoId = ProductoId.generar();

		applicationEventPublisher.publishEvent(new ProductoCreadoEvento(productoId, Instant.now()));

		await().atMost(Duration.ofSeconds(10))
				.untilAsserted(() -> assertThat(output)
						.contains("Producto creado (vía mensajería asíncrona): " + productoId.valor()));
	}
}
