package br.dac.bantads.ms_conta.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String FILA_REGISTRO_CONTA_CLIENTE = "FILA_REGISTRO_CONTA_CLIENTE";
    public static final String FILA_ERRO_NOVO_CLIENTE = "FILA_ERRO_NOVO_CLIENTE";
    public static final String FILA_UPDATE_CONTA = "FILA_UPDATE_CONTA";
    public static final String FILA_ATRIBUI_CONTA_GERENTE = "FILA_ATRIBUI_CONTA_GERENTE";
    public static final String FILA_DISTRIBUI_CONTAS_GERENTE = "FILA_DISTRIBUI_CONTAS_GERENTE";
    public static final String FILA_NOTIFICA_UPDATE_CONTA = "FILA_NOTIFICA_UPDATE_CONTA";

    @Bean
    public Queue registroContaClienteQueue() {
        return new Queue(FILA_REGISTRO_CONTA_CLIENTE, true);
    }

    @Bean
    public Queue erroNovoClienteQueue() {
        return new Queue(FILA_ERRO_NOVO_CLIENTE, true);
    }

    @Bean
    public Queue updateContaQueue() {
        return new Queue(FILA_UPDATE_CONTA, true);
    }

    @Bean
    public Queue atribuiContaGerenteQueue() {
        return new Queue(FILA_ATRIBUI_CONTA_GERENTE, true);
    }

    @Bean
    public Queue distribuiContasGerenteQueue() {
        return new Queue(FILA_DISTRIBUI_CONTAS_GERENTE, true);
    }

    @Bean
    public Queue notificaUpdateContaQueue() {
        return new Queue(FILA_NOTIFICA_UPDATE_CONTA, true);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder().findAndAddModules().build();
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
