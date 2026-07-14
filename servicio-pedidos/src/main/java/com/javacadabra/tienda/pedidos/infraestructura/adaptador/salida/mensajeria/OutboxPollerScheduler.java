package com.javacadabra.tienda.pedidos.infraestructura.adaptador.salida.mensajeria;

import com.javacadabra.tienda.pedidos.infraestructura.adaptador.salida.persistencia.entidad.OutboxEventoEntidad;
import com.javacadabra.tienda.pedidos.infraestructura.adaptador.salida.persistencia.repositorio.OutboxEventoRepositorioJpa;
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

// outbox.poller.enabled=false en los @SpringBootTest de contexto completo que no necesitan un broker real.
@Slf4j
@Component
@ConditionalOnProperty(prefix = "outbox.poller", name = "enabled", matchIfMissing = true)
@RequiredArgsConstructor
public class OutboxPollerScheduler {

	private final OutboxEventoRepositorioJpa outboxEventoRepositorioJpa;
	private final StreamBridge streamBridge;

	@Scheduled(fixedDelay = 2000)
	@Transactional
	public void publicarEventosPendientes() {
		for (OutboxEventoEntidad evento : outboxEventoRepositorioJpa.findByPublicadoFalseOrderByIdAsc()) {
			streamBridge.send("pedidoCreado-out-0", MessageBuilder.withPayload(evento.getPayload())
					.setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
					.build());
			outboxEventoRepositorioJpa.save(new OutboxEventoEntidad(
					evento.getId(), evento.getTipoEvento(), evento.getPayload(), evento.getOcurridoEn(), true));
			log.info("Evento outbox {} publicado en pedido-creado", evento.getId());
		}
	}
}
