package br.dac.bantads.ms_cliente.infrastructure.email;

import br.dac.bantads.ms_cliente.application.service.MailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Implementação de {@link MailService} que NÃO envia e-mail diretamente.
 *
 * Todo envio é centralizado no ms_notificacao: aqui apenas publicamos o
 * envelope genérico {destino, assunto, mensagem} na fila {@code FILA_EMAIL}
 * (Topic Exchange {@code bantads.topic}, String JSON crua). Assim este serviço
 * não fala com SMTP. Publicação é não-fatal: uma falha apenas loga.
 */
@Service
@Slf4j
public class MailServiceImpl implements MailService {

    private static final String EXCHANGE = "bantads.topic";
    private static final String FILA_EMAIL = "FILA_EMAIL";

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    public MailServiceImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void sendMail(String to, String subject, String text) {
        try {
            String payload = objectMapper.writeValueAsString(
                    Map.of("destino", to, "assunto", subject, "mensagem", text));
            rabbitTemplate.convertAndSend(EXCHANGE, FILA_EMAIL, payload);
            log.debug("Solicitação de e-mail enfileirada p/ ms_notificacao: {} ('{}')", to, subject);
        } catch (Exception e) {
            log.warn("Falha ao enfileirar e-mail para {} (assunto '{}'): {}. Fluxo continua.",
                    to, subject, e.getMessage());
        }
    }
}
