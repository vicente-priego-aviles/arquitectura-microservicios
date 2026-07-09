package com.javacadabra.tienda.pedidos;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(
		title = "Servicio de Pedidos",
		description = "Creación de pedidos de clientes",
		version = "v1"))
public class ServicioPedidosApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServicioPedidosApplication.class, args);
	}

}
