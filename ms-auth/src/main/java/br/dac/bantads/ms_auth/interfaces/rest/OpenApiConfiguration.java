package br.dac.bantads.ms_auth.interfaces.rest;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "BANTADS - ms-auth API",
        version = "v1",
        description = "Microsserviço de autenticação",
        contact = @Contact(name = "BANTADS")
    ),
    servers = {
        @Server(url = "http://localhost:${server.port}", description = "Ambiente local")
    }
)
public class OpenApiConfiguration {
}
