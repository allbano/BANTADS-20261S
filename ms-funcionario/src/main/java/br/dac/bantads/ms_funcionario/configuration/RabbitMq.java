package br.dac.bantads.ms_funcionario.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMq {
    private final String queueName = "funcionario-queue";

    @Bean
    public Queue funcionarioQueue() {
        return new Queue(queueName, true);
    }
}