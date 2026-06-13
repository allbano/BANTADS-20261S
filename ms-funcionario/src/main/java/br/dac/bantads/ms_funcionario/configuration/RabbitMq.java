package br.dac.bantads.ms_funcionario.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
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
 * Topologia RabbitMQ do ms-funcionario (gerentes).
 *
 * Exchange unico {@code bantads.topic} (Topic, durable); routing key = nome
 * logico da fila. Declara e faz binding apenas das filas que possui um
 * {@code @RabbitListener}; as demais constantes sao routing keys de publicacao.
 */
@Configuration
public class RabbitMq {

    public static final String EXCHANGE = "bantads.topic";
    public static final String DLX = "bantads.dlx";

    // Filas consumidas por este servico (declaradas + bound aqui)
    public static final String FILA_FUNCIONARIO = "funcionario-queue";
    public static final String FILA_CREATE_GERENTE = "FILA_CREATE_GERENTE";
    public static final String FILA_DELETE_GERENTE = "FILA_DELETE_GERENTE";

    // Routing keys usadas apenas para publicar (declaradas pelos consumidores)
    public static final String FILA_ATRIBUI_CONTA_GERENTE = "FILA_ATRIBUI_CONTA_GERENTE";
    public static final String FILA_AUTENTICACAO = "FILA_AUTENTICACAO";

    @Bean
    public TopicExchange bantadsTopic() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    public DirectExchange bantadsDlx() {
        return ExchangeBuilder.directExchange(DLX).durable(true).build();
    }

    @Bean Queue funcionarioQueue()   { return dlqEnabled(FILA_FUNCIONARIO); }
    @Bean Queue createGerenteQueue() { return dlqEnabled(FILA_CREATE_GERENTE); }
    @Bean Queue deleteGerenteQueue() { return dlqEnabled(FILA_DELETE_GERENTE); }

    @Bean Queue funcionarioDlq()   { return dlq(FILA_FUNCIONARIO); }
    @Bean Queue createGerenteDlq() { return dlq(FILA_CREATE_GERENTE); }
    @Bean Queue deleteGerenteDlq() { return dlq(FILA_DELETE_GERENTE); }

    @Bean Binding bFuncionario()   { return bind(funcionarioQueue(), FILA_FUNCIONARIO); }
    @Bean Binding bCreateGerente() { return bind(createGerenteQueue(), FILA_CREATE_GERENTE); }
    @Bean Binding bDeleteGerente() { return bind(deleteGerenteQueue(), FILA_DELETE_GERENTE); }

    @Bean Binding bDlqFuncionario()   { return bindDlq(funcionarioDlq(), FILA_FUNCIONARIO); }
    @Bean Binding bDlqCreateGerente() { return bindDlq(createGerenteDlq(), FILA_CREATE_GERENTE); }
    @Bean Binding bDlqDeleteGerente() { return bindDlq(deleteGerenteDlq(), FILA_DELETE_GERENTE); }

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
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
