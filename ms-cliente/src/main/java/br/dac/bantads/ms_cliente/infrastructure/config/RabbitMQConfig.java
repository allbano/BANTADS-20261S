package br.dac.bantads.ms_cliente.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String FILA_REGISTRO_CLIENTE = "FILA_REGISTRO_CLIENTE";
    public static final String FILA_ERRO_NOVO_CLIENTE = "FILA_ERRO_NOVO_CLIENTE";
    public static final String FILA_UPDATE_CLIENTE = "FILA_UPDATE_CLIENTE";
    public static final String FILA_ERRO_UPDATE_CLIENTE = "FILA_ERRO_UPDATE_CLIENTE";
    public static final String FILA_ERRO_NOVO_CLIENTE_AUTENTICACAO = "FILA_ERRO_NOVO_CLIENTE_AUTENTICACAO";
    public static final String FILA_NOTIFICA_UPDATE_CONTA = "FILA_NOTIFICA_UPDATE_CONTA";
    public static final String FILA_AUTENTICACAO = "FILA_AUTENTICACAO";

    // Filas do padrão Saga
    public static final String SAGA_CMD_EXCLUIR_CLIENTE = "SAGA_CMD_EXCLUIR_CLIENTE";
    public static final String SAGA_EVT_CLIENTE_CRIADO  = "SAGA_EVT_CLIENTE_CRIADO";
    public static final String SAGA_EVT_CLIENTE_ERRO    = "SAGA_EVT_CLIENTE_ERRO";

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder().findAndAddModules().build();
    }

    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public Queue filaRegistroCliente() {
        return new Queue(FILA_REGISTRO_CLIENTE, true);
    }

    @Bean
    public Queue filaErroNovoCliente() {
        return new Queue(FILA_ERRO_NOVO_CLIENTE, true);
    }

    @Bean
    public Queue filaUpdateCliente() {
        return new Queue(FILA_UPDATE_CLIENTE, true);
    }

    @Bean
    public Queue filaErroUpdateCliente() {
        return new Queue(FILA_ERRO_UPDATE_CLIENTE, true);
    }

    @Bean
    public Queue filaErroNovoClienteAutenticacao() {
        return new Queue(FILA_ERRO_NOVO_CLIENTE_AUTENTICACAO, true);
    }

    @Bean
    public Queue filaNotificaUpdateConta() {
        return new Queue(FILA_NOTIFICA_UPDATE_CONTA, true);
    }

    @Bean
    public Queue filaAutenticacao() {
        return new Queue(FILA_AUTENTICACAO, true);
    }

    @Bean
    public Queue sagaCmdExcluirClienteQueue() {
        return new Queue(SAGA_CMD_EXCLUIR_CLIENTE, true);
    }

    @Bean
    public Queue sagaEvtClienteCriadoQueue() {
        return new Queue(SAGA_EVT_CLIENTE_CRIADO, true);
    }

    @Bean
    public Queue sagaEvtClienteErroQueue() {
        return new Queue(SAGA_EVT_CLIENTE_ERRO, true);
    }
}
