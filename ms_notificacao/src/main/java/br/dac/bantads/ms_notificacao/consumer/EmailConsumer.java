package br.dac.bantads.ms_notificacao.consumer;

import br.dac.bantads.ms_notificacao.config.RabbitMQConfig;
import br.dac.bantads.ms_notificacao.dto.EmailDTO;
import br.dac.bantads.ms_notificacao.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor da fila genérica {@code FILA_EMAIL}.
 *
 * Centraliza o envio: recebe {@link EmailDTO} {destino, assunto, mensagem}
 * publicado por qualquer microsserviço e dispara o e-mail. Falha de SMTP é
 * logada e a mensagem é considerada consumida (ack) — não derruba ninguém.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailConsumer {

    private final EmailService emailService;
    private final ObjectMapper notificacaoObjectMapper;

    @RabbitListener(queues = RabbitMQConfig.FILA_EMAIL)
    public void receber(String mensagem) {
        log.info("Mensagem recebida em {}: {}", RabbitMQConfig.FILA_EMAIL, mensagem);
        try {
            EmailDTO email = notificacaoObjectMapper.readValue(mensagem, EmailDTO.class);
            if (email.getDestino() == null || email.getDestino().isBlank()) {
                log.warn("E-mail sem 'destino' ignorado: {}", mensagem);
                return;
            }
            emailService.enviarSimples(email.getDestino(), email.getAssunto(), email.getMensagem());
        } catch (Exception e) {
            log.error("Falha ao processar/enviar e-mail [{}]: {}", mensagem, e.getMessage(), e);
        }
    }
}
