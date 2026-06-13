package br.dac.bantads.ms_notificacao.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracao de mensageria do ms_notificacao.
 *
 * Segue a convencao ja usada pelos demais microsservicos do BANTADS: filas
 * "diretas" e nomeadas (sem exchange explicito -> exchange default do RabbitMQ,
 * com routing key = nome da fila) e payload em JSON String.
 *
 * Aqui declaramos a UNICA fila que este servico escuta: {@code FILA_NOTIFICACAO}.
 * Como a fila e durable, ela sobrevive a reinicializacoes do broker e as
 * mensagens nao se perdem enquanto nao forem consumidas.
 */
@Configuration
public class RabbitMQConfig {

    /** Nome da fila de notificacoes. Publicadores devem usar exatamente este nome. */
    public static final String FILA_NOTIFICACAO = "FILA_NOTIFICACAO";

    /**
     * Declara a fila no broker (idempotente: se ja existir com os mesmos
     * parametros, nada muda). O segundo argumento {@code true} marca a fila
     * como durable.
     */
    @Bean
    Queue notificacaoQueue() {
        return new Queue(FILA_NOTIFICACAO, true);
    }

    /**
     * ObjectMapper dedicado para desserializar o JSON recebido na fila.
     * {@code findAndAddModules()} registra modulos extras no classpath
     * (ex.: datas do java.time), evitando erros de parsing.
     */
    @Bean
    ObjectMapper notificacaoObjectMapper() {
        return JsonMapper.builder().findAndAddModules().build();
    }
}
