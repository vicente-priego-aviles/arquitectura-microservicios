package com.javacadabra.tienda.pedidos.infraestructura.adaptador.entrada.mensajeria;

import com.javacadabra.tienda.pedidos.aplicacion.puerto.entrada.ConfirmarPedidoPuertoEntrada;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
public class StockReservadoConsumidorConfiguracion {

	private final ConfirmarPedidoPuertoEntrada confirmarPedidoPuertoEntrada;

	@Bean
	public Consumer<StockReservadoEventoDTO> stockReservadoConsumidor() {
		return evento -> confirmarPedidoPuertoEntrada.confirmar(evento.pedidoId());
	}
}
