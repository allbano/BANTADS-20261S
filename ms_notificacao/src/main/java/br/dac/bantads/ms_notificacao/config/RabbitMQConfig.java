package br.dac.bantads.ms_notificacao.config;

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
 * Configuracao de mensageria do ms_notificacao.
 *
 * Topologia padronizada do BANTADS: exchange unico {@code bantads.topic}
 * (Topic, durable), routing key = nome logico da fila, e Dead Letter Exchange
 * {@code bantads.dlx} para mensagens que falham no processamento.
 *
 * Este servico consome unicamente {@code FILA_NOTIFICACAO}.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "bantads.topic";
    public static final String DLX = "bantads.dlx";

    /** Nome da fila de notificacoes. Publicadores usam esta routing key no exchange. */
    public static final String FILA_NOTIFICACAO = "FILA_NOTIFICACAO";

    @Bean
    TopicExchange bantadsTopic() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    DirectExchange bantadsDlx() {
        return ExchangeBuilder.directExchange(DLX).durable(true).build();
    }

    @Bean
    Queue notificacaoQueue() {
        return QueueBuilder.durable(FILA_NOTIFICACAO)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", FILA_NOTIFICACAO + ".dlq")
                .build();
    }

    @Bean
    Queue notificacaoDlq() {
        return QueueBuilder.durable(FILA_NOTIFICACAO + ".dlq").build();
    }

    @Bean
    Binding notificacaoBinding() {
        return BindingBuilder.bind(notificacaoQueue()).to(bantadsTopic()).with(FILA_NOTIFICACAO);
    }

    @Bean
    Binding notificacaoDlqBinding() {
        return BindingBuilder.bind(notificacaoDlq()).to(bantadsDlx()).with(FILA_NOTIFICACAO + ".dlq");
    }

    @Bean
    ObjectMapper notificacaoObjectMapper() {
        return JsonMapper.builder().findAndAddModules().build();
    }
}
