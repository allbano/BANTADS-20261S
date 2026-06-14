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

    /** Nome da fila de notificacoes (DTO tipado: APROVACAO/REJEICAO/...). */
    public static final String FILA_NOTIFICACAO = "FILA_NOTIFICACAO";

    /**
     * Fila de e-mail GENÉRICA {destino, assunto, mensagem}. É o ponto único pelo
     * qual qualquer microsserviço solicita o envio de um e-mail: o produtor monta
     * o conteúdo e publica aqui; SOMENTE o ms_notificacao fala com o SMTP.
     */
    public static final String FILA_EMAIL = "FILA_EMAIL";

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

    // ── Fila de e-mail genérica {destino, assunto, mensagem} ──
    @Bean
    Queue emailQueue() {
        return QueueBuilder.durable(FILA_EMAIL)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", FILA_EMAIL + ".dlq")
                .build();
    }

    @Bean
    Queue emailDlq() {
        return QueueBuilder.durable(FILA_EMAIL + ".dlq").build();
    }

    @Bean
    Binding emailBinding() {
        return BindingBuilder.bind(emailQueue()).to(bantadsTopic()).with(FILA_EMAIL);
    }

    @Bean
    Binding emailDlqBinding() {
        return BindingBuilder.bind(emailDlq()).to(bantadsDlx()).with(FILA_EMAIL + ".dlq");
    }

    @Bean
    ObjectMapper notificacaoObjectMapper() {
        return JsonMapper.builder().findAndAddModules().build();
    }
}
