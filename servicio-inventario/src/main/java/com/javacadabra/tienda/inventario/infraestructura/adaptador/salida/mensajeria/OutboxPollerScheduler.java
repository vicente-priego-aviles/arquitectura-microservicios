package com.javacadabra.tienda.inventario.infraestructura.adaptador.salida.mensajeria;

import com.javacadabra.tienda.inventario.dominio.evento.ReservaStockRechazadaEvento;
import com.javacadabra.tienda.inventario.dominio.evento.StockReservadoEvento;
import com.javacadabra.tienda.inventario.infraestructura.adaptador.salida.persistencia.entidad.OutboxEventoEntidad;
import com.javacadabra.tienda.inventario.infraestructura.adaptador.salida.persistencia.repositorio.OutboxEventoRepositorioJpa;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MimeTypeUtils;

import java.util.Map;

// outbox.poller.enabled=false en los @SpringBootTest de contexto completo que no necesitan un broker real.
@Slf4j
@Component
@ConditionalOnProperty(prefix = "outbox.poller", name = "enabled", matchIfMissing = true)
@RequiredArgsConstructor
public class OutboxPollerScheduler {

	// A diferencia del poller de servicio-pedidos (un único tipo de evento, un único destino fijo),
	// aquí hay dos tipos posibles y cada uno va a un binding/topic distinto.
	private static final Map<String, String> BINDING_POR_TIPO_EVENTO = Map.of(
			StockReservadoEvento.class.getSimpleName(), "stockReservado-out-0",
			ReservaStockRechazadaEvento.class.getSimpleName(), "reservaStockRechazada-out-0");

	private final OutboxEventoRepositorioJpa outboxEventoRepositorioJpa;
	private final StreamBridge streamBridge;

	@Scheduled(fixedDelay = 2000)
	@Transactional
	public void publicarEventosPendientes() {
		for (OutboxEventoEntidad evento : outboxEventoRepositorioJpa.findByPublicadoFalseOrderByIdAsc()) {
			String binding = BINDING_POR_TIPO_EVENTO.get(evento.getTipoEvento());
			streamBridge.send(binding, MessageBuilder.withPayload(evento.getPayload())
					.setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
					.build());
			evento.marcarPublicado();
			log.info("Evento outbox {} ({}) publicado", evento.getId(), evento.getTipoEvento());
		}
	}
}
