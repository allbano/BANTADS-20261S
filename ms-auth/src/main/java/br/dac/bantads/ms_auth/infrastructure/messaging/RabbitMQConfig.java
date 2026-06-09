package br.dac.bantads.ms_auth.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Fila que ms-cliente e ms-funcionario já publicam (auth escuta aqui)
    public static final String FILA_AUTENTICACAO = "FILA_AUTENTICACAO";

    // Eventos de resposta para o saga orquestrador
    public static final String SAGA_EVT_AUTH_CRIADO = "SAGA_EVT_AUTH_CRIADO";
    public static final String SAGA_EVT_AUTH_ERRO   = "SAGA_EVT_AUTH_ERRO";

    @Bean Queue autenticacaoQueue()      { return new Queue(FILA_AUTENTICACAO, true); }
    @Bean Queue sagaEvtAuthCriadoQueue() { return new Queue(SAGA_EVT_AUTH_CRIADO, true); }
    @Bean Queue sagaEvtAuthErroQueue()   { return new Queue(SAGA_EVT_AUTH_ERRO, true); }

    @Bean
    ObjectMapper authObjectMapper() {
        return JsonMapper.builder().findAndAddModules().build();
    }
}
