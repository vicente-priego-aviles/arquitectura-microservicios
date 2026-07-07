package com.javacadabra.tienda.catalogo;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(
		title = "Servicio de Catálogo",
		description = "Gestión de productos y categorías del catálogo",
		version = "v1"))
public class ServicioCatalogoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServicioCatalogoApplication.class, args);
	}

}
