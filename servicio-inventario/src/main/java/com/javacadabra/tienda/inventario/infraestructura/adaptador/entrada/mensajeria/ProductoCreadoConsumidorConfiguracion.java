package com.javacadabra.tienda.inventario.infraestructura.adaptador.entrada.mensajeria;

import com.javacadabra.tienda.inventario.aplicacion.puerto.entrada.CrearStockPuertoEntrada;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
public class ProductoCreadoConsumidorConfiguracion {

	private final CrearStockPuertoEntrada crearStockPuertoEntrada;

	@Bean
	public Consumer<ProductoCreadoEventoDTO> productoCreadoConsumidor() {
		return evento -> crearStockPuertoEntrada.crear(evento.productoId().valor());
	}
}
