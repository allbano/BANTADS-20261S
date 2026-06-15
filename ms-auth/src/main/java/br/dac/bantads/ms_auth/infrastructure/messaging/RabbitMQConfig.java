package br.dac.bantads.ms_auth.infrastructure.messaging;

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
 * Topologia RabbitMQ do ms-auth.
 *
 * Toda a comunicacao assincrona do BANTADS passa por um unico Topic Exchange
 * durable ({@code bantads.topic}); a routing key usada e o proprio nome logico
 * da fila. Cada microsservico declara e faz binding APENAS das filas que ele
 * consome (regra de ownership) — isso evita declaracoes divergentes da mesma
 * fila em servicos diferentes (PRECONDITION_FAILED).
 *
 * Falhas de processamento sao encaminhadas para o Dead Letter Exchange
 * ({@code bantads.dlx}) -> fila {@code <nome>.dlq}.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "bantads.topic";
    public static final String DLX = "bantads.dlx";

    // Fila consumida por este servico
    public static final String FILA_AUTENTICACAO = "FILA_AUTENTICACAO";

    // Eventos publicados por este servico (declarados/bound pelo ms-saga, que consome)
    public static final String SAGA_EVT_AUTH_CRIADO = "SAGA_EVT_AUTH_CRIADO";
    public static final String SAGA_EVT_AUTH_ERRO   = "SAGA_EVT_AUTH_ERRO";

    @Bean
    TopicExchange bantadsTopic() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    DirectExchange bantadsDlx() {
        return ExchangeBuilder.directExchange(DLX).durable(true).build();
    }

    @Bean
    Queue autenticacaoQueue() {
        return QueueBuilder.durable(FILA_AUTENTICACAO)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", FILA_AUTENTICACAO + ".dlq")
                .build();
    }

    @Bean
    Queue autenticacaoDlq() {
        return QueueBuilder.durable(FILA_AUTENTICACAO + ".dlq").build();
    }

    @Bean
    Binding autenticacaoBinding() {
        return BindingBuilder.bind(autenticacaoQueue()).to(bantadsTopic()).with(FILA_AUTENTICACAO);
    }

    @Bean
    Binding autenticacaoDlqBinding() {
        return BindingBuilder.bind(autenticacaoDlq()).to(bantadsDlx()).with(FILA_AUTENTICACAO + ".dlq");
    }

    @Bean
    ObjectMapper authObjectMapper() {
        return JsonMapper.builder().findAndAddModules().build();
    }
}
