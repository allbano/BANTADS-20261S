package br.dac.bantads.ms_auth.infrastructure.messaging;

import br.dac.bantads.ms_auth.application.service.UserAccountService;
import br.dac.bantads.ms_auth.domain.account.AccountRole;
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

import java.security.SecureRandom;
import java.util.Map;

/**
 * Comandos das SAGAs genéricas (Eixo 3) endereçados ao ms-auth: criar credencial
 * (R17), gerar senha aleatória na aprovação (R10), atualizar senha (R20) e
 * remover credencial (R18 / compensação do R17). Responde em {@code saga.reply}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SagaCommandConsumer {

    private static final String EXCHANGE = "bantads.topic";
    private static final String REPLY = "saga.reply";
    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserAccountService userAccountService;
    private final ObjectMapper authObjectMapper;
    private final RabbitTemplate rabbitTemplate;

    // ── R17: criar credencial com a senha do formulário ──
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "saga.cmd.auth.criar", durable = "true"),
            exchange = @Exchange(value = EXCHANGE, type = "topic"),
            key = "saga.cmd.auth.criar"))
    public void criar(String msg) {
        try {
            JsonNode n = authObjectMapper.readTree(msg);
            String sagaId = n.path("sagaId").asText();
            String email = n.path("email").asText();
            String senha = n.path("senha").asText("");
            AccountRole role = role(n.path("cargo").asText("GERENTE"));
            userAccountService.createOrUpdateWithPassword(email, senha, role);
            replyOk(sagaId, Map.of("email", email));
        } catch (Exception e) {
            replyErroSafe(msg, "Falha ao criar credencial: " + e.getMessage());
        }
    }

    // ── R10: gerar senha aleatória e ativar credencial do cliente ──
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "saga.cmd.auth.gerar.senha", durable = "true"),
            exchange = @Exchange(value = EXCHANGE, type = "topic"),
            key = "saga.cmd.auth.gerar.senha"))
    public void gerarSenha(String msg) {
        try {
            JsonNode n = authObjectMapper.readTree(msg);
            String sagaId = n.path("sagaId").asText();
            String email = n.path("email").asText();
            AccountRole role = role(n.path("cargo").asText("CLIENTE"));
            String senhaGerada = gerarSenhaAleatoria();
            userAccountService.createOrUpdateWithPassword(email, senhaGerada, role);
            replyOk(sagaId, Map.of("senhaGerada", senhaGerada, "email", email));
        } catch (Exception e) {
            replyErroSafe(msg, "Falha ao gerar senha: " + e.getMessage());
        }
    }

    // ── R20: atualizar senha (apenas se enviada) ──
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "saga.cmd.auth.atualizar.senha", durable = "true"),
            exchange = @Exchange(value = EXCHANGE, type = "topic"),
            key = "saga.cmd.auth.atualizar.senha"))
    public void atualizarSenha(String msg) {
        try {
            JsonNode n = authObjectMapper.readTree(msg);
            String sagaId = n.path("sagaId").asText();
            String email = n.path("email").asText();
            String senha = n.path("senha").asText("");
            if (!senha.isBlank()) {
                userAccountService.createOrUpdateWithPassword(email, senha, role(n.path("cargo").asText("GERENTE")));
            }
            replyOk(sagaId, Map.of());
        } catch (Exception e) {
            replyErroSafe(msg, "Falha ao atualizar senha: " + e.getMessage());
        }
    }

    // ── R18 / compensação do R17: remover credencial ──
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "saga.cmd.auth.remover", durable = "true"),
            exchange = @Exchange(value = EXCHANGE, type = "topic"),
            key = "saga.cmd.auth.remover"))
    public void remover(String msg) {
        try {
            JsonNode n = authObjectMapper.readTree(msg);
            String sagaId = n.path("sagaId").asText();
            String email = n.path("email").asText(null);
            if (email != null && !email.isBlank()) {
                try {
                    userAccountService.deleteAccountByEmail(email);
                } catch (Exception ignored) {
                    log.warn("Credencial inexistente ao remover: {}", email);
                }
            }
            replyOk(sagaId, Map.of());
        } catch (Exception e) {
            replyErroSafe(msg, "Falha ao remover credencial: " + e.getMessage());
        }
    }

    private AccountRole role(String cargo) {
        try { return AccountRole.fromValue(cargo); }
        catch (Exception e) { return AccountRole.CLIENTE; }
    }

    private String gerarSenhaAleatoria() {
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) sb.append(ALFABETO.charAt(RANDOM.nextInt(ALFABETO.length())));
        return sb.toString();
    }

    private void replyOk(String sagaId, Map<String, Object> dados) {
        publicar(Map.of("sagaId", sagaId, "sucesso", true, "dados", dados));
    }

    private void replyErroSafe(String originalMsg, String motivo) {
        try {
            String sagaId = authObjectMapper.readTree(originalMsg).path("sagaId").asText();
            publicar(Map.of("sagaId", sagaId, "sucesso", false, "mensagem", motivo));
        } catch (Exception e) {
            log.error("Falha ao publicar erro saga.reply", e);
        }
    }

    private void publicar(Map<String, Object> reply) {
        try {
            rabbitTemplate.convertAndSend(EXCHANGE, REPLY, authObjectMapper.writeValueAsString(reply));
        } catch (Exception e) {
            log.error("Falha ao publicar saga.reply", e);
        }
    }
}
