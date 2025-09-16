package com.challenge.meli.inventory.config;

import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sistema de Inventario Distribuido API")
                        .version("1.0.0")
                        .description("APIs para gestión de inventario en tiempo real con reservas y consultas de disponibilidad")
                        .contact(new Contact()
                                .name("Carlos Correa")
                                .email("carlos.correa.zapata@gmail.com"))
                        .license(new License()
                                .name("NA")
                                .url("xxxxxx")));
    }
}
