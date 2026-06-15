package br.dac.bantads.ms_notificacao.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracao de mensageria do ms_notificacao.
 *
 * Topologia padronizada do BANTADS: exchange unico {@code bantads.topic}
 * (Topic, durable) e Dead Letter Exchange {@code bantads.dlx}.
 *
 * Centralizacao do e-mail: este servico recebe SOMENTE notificacoes tipadas no
 * canal {@code saga.cmd.notificar.cliente} (declarado via {@code @QueueBinding}
 * no {@code NotificacaoSagaConsumer}). Nao ha mais a fila generica de e-mail
 * {@code FILA_EMAIL} nem a {@code FILA_NOTIFICACAO}: o conteudo do e-mail e
 * montado aqui (R1/R10/R11), nunca pelos produtores.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "bantads.topic";
    public static final String DLX = "bantads.dlx";

    @Bean
    TopicExchange bantadsTopic() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    DirectExchange bantadsDlx() {
        return ExchangeBuilder.directExchange(DLX).durable(true).build();
    }

    @Bean
    ObjectMapper notificacaoObjectMapper() {
        return JsonMapper.builder().findAndAddModules().build();
    }
}
