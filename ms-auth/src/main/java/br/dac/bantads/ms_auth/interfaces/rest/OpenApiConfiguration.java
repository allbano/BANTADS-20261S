package br.dac.bantads.ms_auth.interfaces.rest;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "ms-auth API",
                version = "v1",
                description = "API de autenticacao com MongoDB e JWT",
                contact = @Contact(name = "BANTADS")
        ),
        servers = {
                @Server(url = "http://localhost:8085", description = "Ambiente local")
        }
)
public class OpenApiConfiguration {
}
