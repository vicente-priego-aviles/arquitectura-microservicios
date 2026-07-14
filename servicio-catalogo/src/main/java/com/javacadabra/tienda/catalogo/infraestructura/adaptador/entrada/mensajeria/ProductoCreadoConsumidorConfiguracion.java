package com.javacadabra.tienda.catalogo.infraestructura.adaptador.entrada.mensajeria;

import com.javacadabra.tienda.catalogo.dominio.evento.ProductoCreadoEvento;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Slf4j
@Configuration
public class ProductoCreadoConsumidorConfiguracion {

	@Bean
	public Consumer<ProductoCreadoEvento> productoCreadoConsumidor() {
		return evento -> log.info("Producto creado (vía mensajería asíncrona): {}", evento.productoId().valor());
	}
}
