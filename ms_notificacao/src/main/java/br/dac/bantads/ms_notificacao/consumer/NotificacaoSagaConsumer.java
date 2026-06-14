package br.dac.bantads.ms_notificacao.consumer;

import br.dac.bantads.ms_notificacao.dto.NotificacaoDTO;
import br.dac.bantads.ms_notificacao.dto.TipoNotificacao;
import br.dac.bantads.ms_notificacao.service.EmailService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Passo de notificação das SAGAs genéricas (Eixo 3) e do Rejeitar (R11).
 *
 * Recebe {@code saga.cmd.notificar.cliente} carregando o snapshot do contexto
 * (email, nome, senhaGerada/senha, motivo) e dispara o e-mail apropriado. O
 * envio é NÃO-FATAL: se o SMTP falhar, logamos e ainda assim respondemos sucesso
 * (notificação não desfaz a operação). Só responde em {@code saga.reply} quando
 * há {@code sagaId} (o R11 publica sem sagaId — apenas e-mail).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificacaoSagaConsumer {

    private static final String EXCHANGE = "bantads.topic";
    private static final String REPLY = "saga.reply";

    private final EmailService emailService;
    private final ObjectMapper notificacaoObjectMapper;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "saga.cmd.notificar.cliente", durable = "true"),
            exchange = @Exchange(value = EXCHANGE, type = "topic"),
            key = "saga.cmd.notificar.cliente"))
    public void notificar(String msg) {
        String sagaId = null;
        try {
            JsonNode n = notificacaoObjectMapper.readTree(msg);
            sagaId = n.path("sagaId").asText(null);
            if (sagaId != null && sagaId.isBlank()) sagaId = null;

            String senha = n.hasNonNull("senhaGerada") ? n.get("senhaGerada").asText()
                         : n.path("senha").asText(null);
            String tipoStr = n.hasNonNull("tipo") ? n.get("tipo").asText()
                           : (senha != null ? "APROVACAO" : "FALHA_AUTOCADASTRO");

            NotificacaoDTO dto = new NotificacaoDTO(
                    TipoNotificacao.valueOf(tipoStr.toUpperCase()),
                    n.path("email").asText(null),
                    n.path("nome").asText(null),
                    senha,
                    n.path("motivo").asText(null));

            try {
                emailService.enviar(dto);
            } catch (Exception mailEx) {
                log.warn("Envio de e-mail falhou (não-fatal): {}", mailEx.getMessage());
            }
        } catch (Exception e) {
            log.error("Erro ao processar saga.cmd.notificar.cliente: {}", e.getMessage(), e);
        } finally {
            if (sagaId != null) replyOk(sagaId);
        }
    }

    private void replyOk(String sagaId) {
        try {
            rabbitTemplate.convertAndSend(EXCHANGE, REPLY,
                    notificacaoObjectMapper.writeValueAsString(
                            Map.of("sagaId", sagaId, "sucesso", true, "dados", Map.of())));
        } catch (Exception e) {
            log.error("Falha ao publicar saga.reply", e);
        }
    }
}
