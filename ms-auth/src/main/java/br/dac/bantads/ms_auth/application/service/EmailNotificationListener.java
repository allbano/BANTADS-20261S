package br.dac.bantads.ms_auth.application.service;

import br.dac.bantads.ms_auth.application.event.RandomPasswordGeneratedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Em vez de enviar e-mail diretamente, o ms-auth publica o envelope genérico
 * {destino, assunto, mensagem} na fila {@code FILA_EMAIL}; SOMENTE o
 * ms_notificacao fala com o SMTP. Publicação não-fatal.
 */
@Service
@Slf4j
public class EmailNotificationListener {

    private static final String EXCHANGE = "bantads.topic";
    private static final String FILA_EMAIL = "FILA_EMAIL";

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper authObjectMapper;

    public EmailNotificationListener(RabbitTemplate rabbitTemplate, ObjectMapper authObjectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.authObjectMapper = authObjectMapper;
    }

    @EventListener
    public void handleRandomPasswordGeneratedEvent(RandomPasswordGeneratedEvent event) {
        try {
            String payload = authObjectMapper.writeValueAsString(Map.of(
                    "destino", event.email(),
                    "assunto", "BANTADS - Sua senha de acesso",
                    "mensagem", "Sua conta foi aprovada! Sua senha temporária de acesso é: " + event.password()));
            rabbitTemplate.convertAndSend(EXCHANGE, FILA_EMAIL, payload);
        } catch (Exception e) {
            log.warn("Falha ao enfileirar e-mail de senha para {}: {}. Fluxo continua.",
                    event.email(), e.getMessage());
        }
    }
}
