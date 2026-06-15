package br.dac.bantads.ms_cliente.infrastructure.config;

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
 * Topologia RabbitMQ do ms-cliente.
 *
 * Exchange unico {@code bantads.topic} (Topic, durable); routing key = nome
 * logico da fila. Regra de ownership: declara e faz binding apenas das filas
 * que possui um {@code @RabbitListener}. As demais constantes sao apenas
 * routing keys usadas em publicacao.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "bantads.topic";
    public static final String DLX = "bantads.dlx";

    // Filas consumidas por este servico (declaradas + bound aqui)
    public static final String FILA_REGISTRO_CLIENTE = "FILA_REGISTRO_CLIENTE";
    public static final String SAGA_CMD_EXCLUIR_CLIENTE = "SAGA_CMD_EXCLUIR_CLIENTE";

    // Routing keys usadas apenas para publicar (declaradas pelos consumidores)
    public static final String FILA_ERRO_NOVO_CLIENTE = "FILA_ERRO_NOVO_CLIENTE";
    public static final String FILA_ERRO_NOVO_CLIENTE_AUTENTICACAO = "FILA_ERRO_NOVO_CLIENTE_AUTENTICACAO";
    public static final String FILA_AUTENTICACAO = "FILA_AUTENTICACAO";
    public static final String SAGA_EVT_CLIENTE_CRIADO  = "SAGA_EVT_CLIENTE_CRIADO";
    public static final String SAGA_EVT_CLIENTE_ERRO    = "SAGA_EVT_CLIENTE_ERRO";

    @Bean
    TopicExchange bantadsTopic() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    DirectExchange bantadsDlx() {
        return ExchangeBuilder.directExchange(DLX).durable(true).build();
    }

    @Bean Queue filaRegistroCliente()     { return dlqEnabled(FILA_REGISTRO_CLIENTE); }
    @Bean Queue sagaCmdExcluirCliente()   { return dlqEnabled(SAGA_CMD_EXCLUIR_CLIENTE); }

    @Bean Queue filaRegistroClienteDlq()     { return dlq(FILA_REGISTRO_CLIENTE); }
    @Bean Queue sagaCmdExcluirClienteDlq()   { return dlq(SAGA_CMD_EXCLUIR_CLIENTE); }

    @Bean Binding bRegistroCliente()     { return bind(filaRegistroCliente(), FILA_REGISTRO_CLIENTE); }
    @Bean Binding bSagaCmdExcluirCliente() { return bind(sagaCmdExcluirCliente(), SAGA_CMD_EXCLUIR_CLIENTE); }

    @Bean Binding bDlqRegistroCliente()     { return bindDlq(filaRegistroClienteDlq(), FILA_REGISTRO_CLIENTE); }
    @Bean Binding bDlqSagaCmdExcluirCliente() { return bindDlq(sagaCmdExcluirClienteDlq(), SAGA_CMD_EXCLUIR_CLIENTE); }

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
        return JsonMapper.builder().findAndAddModules().build();
    }
}
