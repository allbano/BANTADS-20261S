package br.dac.bantads.ms_funcionario.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMq {

    public static final String FILA_FUNCIONARIO = "funcionario-queue";
    public static final String FILA_CREATE_GERENTE = "FILA_CREATE_GERENTE";
    public static final String FILA_ATRIBUI_CONTA_GERENTE = "FILA_ATRIBUI_CONTA_GERENTE";
    public static final String FILA_DELETE_GERENTE = "FILA_DELETE_GERENTE";
    public static final String FILA_AUTENTICACAO = "FILA_AUTENTICACAO";

    @Bean
    public Queue funcionarioQueue() {
        return new Queue(FILA_FUNCIONARIO, true);
    }

    @Bean
    public Queue createGerenteQueue() {
        return new Queue(FILA_CREATE_GERENTE, true);
    }

    @Bean
    public Queue deleteGerenteQueue() {
        return new Queue(FILA_DELETE_GERENTE, true);
    }

    @Bean
    public Queue atribuiContaGerenteQueue() {
        return new Queue(FILA_ATRIBUI_CONTA_GERENTE, true);
    }

    @Bean
    public Queue autenticacaoQueue() {
        return new Queue(FILA_AUTENTICACAO, true);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}