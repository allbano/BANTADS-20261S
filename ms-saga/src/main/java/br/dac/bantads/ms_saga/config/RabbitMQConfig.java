package br.dac.bantads.ms_saga.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topologia RabbitMQ do ms-saga (orquestrador).
 *
 * Exchange unico {@code bantads.topic} (Topic, durable); routing key = nome
 * logico da fila. Regra de ownership: o ms-saga declara e faz binding apenas
 * das filas de EVENTO que ele consome (respostas dos participantes). As filas
 * de COMANDO (FILA_REGISTRO_CLIENTE, etc.) sao declaradas pelos servicos que as
 * consomem; aqui mantemos somente as constantes para publicar com routing key.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "bantads.topic";
    public static final String DLX = "bantads.dlx";

    // Comandos publicados pelo orquestrador (declarados pelos consumidores)
    public static final String FILA_REGISTRO_CLIENTE        = "FILA_REGISTRO_CLIENTE";
    public static final String FILA_REGISTRO_CONTA_CLIENTE  = "FILA_REGISTRO_CONTA_CLIENTE";
    public static final String FILA_AUTENTICACAO            = "FILA_AUTENTICACAO";
    public static final String FILA_ERRO_NOVO_CLIENTE       = "FILA_ERRO_NOVO_CLIENTE";
    public static final String SAGA_CMD_EXCLUIR_CLIENTE     = "SAGA_CMD_EXCLUIR_CLIENTE";

    // Eventos de resposta consumidos pelo orquestrador (declarados/bound aqui)
    public static final String SAGA_EVT_CLIENTE_CRIADO = "SAGA_EVT_CLIENTE_CRIADO";
    public static final String SAGA_EVT_CONTA_CRIADA   = "SAGA_EVT_CONTA_CRIADA";
    public static final String SAGA_EVT_AUTH_CRIADO    = "SAGA_EVT_AUTH_CRIADO";
    public static final String SAGA_EVT_CLIENTE_ERRO   = "SAGA_EVT_CLIENTE_ERRO";
    public static final String SAGA_EVT_CONTA_ERRO     = "SAGA_EVT_CONTA_ERRO";
    public static final String SAGA_EVT_AUTH_ERRO      = "SAGA_EVT_AUTH_ERRO";

    @Bean
    TopicExchange bantadsTopic() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    DirectExchange bantadsDlx() {
        return ExchangeBuilder.directExchange(DLX).durable(true).build();
    }

    @Bean Queue sagaEvtClienteCriadoQueue() { return dlqEnabled(SAGA_EVT_CLIENTE_CRIADO); }
    @Bean Queue sagaEvtContaCriadaQueue()   { return dlqEnabled(SAGA_EVT_CONTA_CRIADA); }
    @Bean Queue sagaEvtAuthCriadoQueue()    { return dlqEnabled(SAGA_EVT_AUTH_CRIADO); }
    @Bean Queue sagaEvtClienteErroQueue()   { return dlqEnabled(SAGA_EVT_CLIENTE_ERRO); }
    @Bean Queue sagaEvtContaErroQueue()     { return dlqEnabled(SAGA_EVT_CONTA_ERRO); }
    @Bean Queue sagaEvtAuthErroQueue()      { return dlqEnabled(SAGA_EVT_AUTH_ERRO); }

    @Bean Queue sagaEvtClienteCriadoDlq() { return dlq(SAGA_EVT_CLIENTE_CRIADO); }
    @Bean Queue sagaEvtContaCriadaDlq()   { return dlq(SAGA_EVT_CONTA_CRIADA); }
    @Bean Queue sagaEvtAuthCriadoDlq()    { return dlq(SAGA_EVT_AUTH_CRIADO); }
    @Bean Queue sagaEvtClienteErroDlq()   { return dlq(SAGA_EVT_CLIENTE_ERRO); }
    @Bean Queue sagaEvtContaErroDlq()     { return dlq(SAGA_EVT_CONTA_ERRO); }
    @Bean Queue sagaEvtAuthErroDlq()      { return dlq(SAGA_EVT_AUTH_ERRO); }

    @Bean Binding bSagaEvtClienteCriado() { return bind(sagaEvtClienteCriadoQueue(), SAGA_EVT_CLIENTE_CRIADO); }
    @Bean Binding bSagaEvtContaCriada()   { return bind(sagaEvtContaCriadaQueue(), SAGA_EVT_CONTA_CRIADA); }
    @Bean Binding bSagaEvtAuthCriado()    { return bind(sagaEvtAuthCriadoQueue(), SAGA_EVT_AUTH_CRIADO); }
    @Bean Binding bSagaEvtClienteErro()   { return bind(sagaEvtClienteErroQueue(), SAGA_EVT_CLIENTE_ERRO); }
    @Bean Binding bSagaEvtContaErro()     { return bind(sagaEvtContaErroQueue(), SAGA_EVT_CONTA_ERRO); }
    @Bean Binding bSagaEvtAuthErro()      { return bind(sagaEvtAuthErroQueue(), SAGA_EVT_AUTH_ERRO); }

    @Bean Binding bDlqSagaEvtClienteCriado() { return bindDlq(sagaEvtClienteCriadoDlq(), SAGA_EVT_CLIENTE_CRIADO); }
    @Bean Binding bDlqSagaEvtContaCriada()   { return bindDlq(sagaEvtContaCriadaDlq(), SAGA_EVT_CONTA_CRIADA); }
    @Bean Binding bDlqSagaEvtAuthCriado()    { return bindDlq(sagaEvtAuthCriadoDlq(), SAGA_EVT_AUTH_CRIADO); }
    @Bean Binding bDlqSagaEvtClienteErro()   { return bindDlq(sagaEvtClienteErroDlq(), SAGA_EVT_CLIENTE_ERRO); }
    @Bean Binding bDlqSagaEvtContaErro()     { return bindDlq(sagaEvtContaErroDlq(), SAGA_EVT_CONTA_ERRO); }
    @Bean Binding bDlqSagaEvtAuthErro()      { return bindDlq(sagaEvtAuthErroDlq(), SAGA_EVT_AUTH_ERRO); }

    private static Queue dlqEnabled(String name) {
        return QueueBuilder.durable(name)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", name + ".dlq")
                .build();
    }

    private static Queue dlq(String name) {
        return QueueBuilder.durable(name + ".dlq").build();
    }

    private Binding bind(Queue queue, String routingKey) {
        return BindingBuilder.bind(queue).to(bantadsTopic()).with(routingKey);
    }

    private Binding bindDlq(Queue queue, String routingKey) {
        return BindingBuilder.bind(queue).to(bantadsDlx()).with(routingKey + ".dlq");
    }

    @Bean
    ObjectMapper objectMapper() {
        return JsonMapper.builder().findAndAddModules().build();
    }
}
