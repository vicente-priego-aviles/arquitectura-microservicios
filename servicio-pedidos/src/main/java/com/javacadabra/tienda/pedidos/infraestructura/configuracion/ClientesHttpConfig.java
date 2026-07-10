package com.javacadabra.tienda.pedidos.infraestructura.configuracion;

import com.javacadabra.tienda.pedidos.infraestructura.adaptador.salida.http.cliente.CatalogoHttpExchange;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@ImportHttpServices(group = "catalogo", types = CatalogoHttpExchange.class)
public class ClientesHttpConfig {
}
