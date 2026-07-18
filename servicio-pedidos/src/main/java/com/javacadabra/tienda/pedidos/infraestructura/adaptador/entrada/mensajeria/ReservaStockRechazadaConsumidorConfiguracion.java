package com.javacadabra.tienda.pedidos.infraestructura.adaptador.entrada.mensajeria;

import com.javacadabra.tienda.pedidos.aplicacion.puerto.entrada.CancelarPedidoPuertoEntrada;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
public class ReservaStockRechazadaConsumidorConfiguracion {

	private final CancelarPedidoPuertoEntrada cancelarPedidoPuertoEntrada;

	@Bean
	public Consumer<ReservaStockRechazadaEventoDTO> reservaStockRechazadaConsumidor() {
		return evento -> cancelarPedidoPuertoEntrada.cancelar(evento.pedidoId(), evento.motivo());
	}
}
