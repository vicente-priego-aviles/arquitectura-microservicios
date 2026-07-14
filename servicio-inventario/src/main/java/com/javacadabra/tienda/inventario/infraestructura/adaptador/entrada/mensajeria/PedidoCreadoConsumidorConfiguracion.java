package com.javacadabra.tienda.inventario.infraestructura.adaptador.entrada.mensajeria;

import com.javacadabra.tienda.inventario.aplicacion.dto.entrada.LineaReservaDTO;
import com.javacadabra.tienda.inventario.aplicacion.puerto.entrada.ReservarStockPuertoEntrada;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
public class PedidoCreadoConsumidorConfiguracion {

	private final ReservarStockPuertoEntrada reservarStockPuertoEntrada;

	@Bean
	public Consumer<PedidoCreadoEventoDTO> pedidoCreadoConsumidor() {
		return evento -> {
			var lineas = evento.lineas().stream()
					.map(linea -> new LineaReservaDTO(linea.productoId().valor(), linea.cantidad().valor()))
					.toList();
			reservarStockPuertoEntrada.reservar(evento.pedidoId().valor(), lineas);
		};
	}
}
