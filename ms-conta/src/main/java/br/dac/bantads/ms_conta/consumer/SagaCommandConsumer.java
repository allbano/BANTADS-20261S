package br.dac.bantads.ms_conta.consumer;

import br.dac.bantads.ms_conta.service.ContaService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Comandos das SAGAs genéricas (Eixo 3) endereçados ao ms-conta: ativar/desativar
 * conta (R10), recalcular limite (R4), atribuir conta a novo gerente (R17) e
 * redistribuir contas de gerente removido (R18). Responde em {@code saga.reply}.
 */
@Component
@Slf4j
public class SagaCommandConsumer {

    private static final String EXCHANGE = "bantads.topic";
    private static final String REPLY = "saga.reply";

    private final ContaService contaService;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public SagaCommandConsumer(ContaService contaService, RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.contaService = contaService;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    // ── R10: ativar conta ──
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "saga.cmd.conta.ativar", durable = "true"),
            exchange = @Exchange(value = EXCHANGE, type = "topic"),
            key = "saga.cmd.conta.ativar"))
    public void ativar(String msg) {
        try {
            JsonNode n = objectMapper.readTree(msg);
            String sagaId = n.path("sagaId").asText();
            UUID uuidCliente = UUID.fromString(n.path("uuidCliente").asText());
            BigDecimal salario = n.hasNonNull("salario") ? n.get("salario").decimalValue() : null;
            String numero = contaService.ativarConta(uuidCliente, salario);
            replyOk(sagaId, Map.of("numeroConta", numero));
        } catch (Exception e) {
            replyErroSafe(msg, "Falha ao ativar conta: " + e.getMessage());
        }
    }

    // ── compensação do ativar ──
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "saga.cmd.conta.desativar", durable = "true"),
            exchange = @Exchange(value = EXCHANGE, type = "topic"),
            key = "saga.cmd.conta.desativar"))
    public void desativar(String msg) {
        try {
            JsonNode n = objectMapper.readTree(msg);
            contaService.desativarConta(UUID.fromString(n.path("uuidCliente").asText()));
            log.info("Compensação: conta do cliente {} desativada", n.path("uuidCliente").asText());
            replyOk(n.path("sagaId").asText(), Map.of());
        } catch (Exception e) {
            log.error("Erro em saga.cmd.conta.desativar: {}", e.getMessage(), e);
        }
    }

    // ── R4: recalcular limite ──
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "saga.cmd.conta.recalcular.limite", durable = "true"),
            exchange = @Exchange(value = EXCHANGE, type = "topic"),
            key = "saga.cmd.conta.recalcular.limite"))
    public void recalcularLimite(String msg) {
        try {
            JsonNode n = objectMapper.readTree(msg);
            String sagaId = n.path("sagaId").asText();
            UUID uuidCliente = UUID.fromString(n.path("uuidCliente").asText());
            BigDecimal salario = n.hasNonNull("salario") ? n.get("salario").decimalValue() : BigDecimal.ZERO;
            contaService.recalcularLimite(uuidCliente, salario);
            replyOk(sagaId, Map.of());
        } catch (Exception e) {
            replyErroSafe(msg, "Falha ao recalcular limite: " + e.getMessage());
        }
    }

    // ── R17: atribuir 1 conta ao novo gerente ──
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "saga.cmd.conta.atribuir.gerente", durable = "true"),
            exchange = @Exchange(value = EXCHANGE, type = "topic"),
            key = "saga.cmd.conta.atribuir.gerente"))
    public void atribuirGerente(String msg) {
        try {
            JsonNode n = objectMapper.readTree(msg);
            String sagaId = n.path("sagaId").asText();
            contaService.atribuiContaGerente(UUID.fromString(n.path("uuidGerente").asText()));
            replyOk(sagaId, Map.of());
        } catch (Exception e) {
            replyErroSafe(msg, "Falha ao atribuir conta ao gerente: " + e.getMessage());
        }
    }

    // ── R18: redistribuir contas do gerente removido ──
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "saga.cmd.conta.redistribuir.gerente", durable = "true"),
            exchange = @Exchange(value = EXCHANGE, type = "topic"),
            key = "saga.cmd.conta.redistribuir.gerente"))
    public void redistribuirGerente(String msg) {
        try {
            JsonNode n = objectMapper.readTree(msg);
            String sagaId = n.path("sagaId").asText();
            contaService.distribuiContasGerente(UUID.fromString(n.path("uuidGerente").asText()));
            replyOk(sagaId, Map.of());
        } catch (Exception e) {
            replyErroSafe(msg, "Falha ao redistribuir contas: " + e.getMessage());
        }
    }

    private void replyOk(String sagaId, Map<String, Object> dados) {
        publicar(Map.of("sagaId", sagaId, "sucesso", true, "dados", dados));
    }

    private void replyErroSafe(String originalMsg, String motivo) {
        try {
            String sagaId = objectMapper.readTree(originalMsg).path("sagaId").asText();
            publicar(Map.of("sagaId", sagaId, "sucesso", false, "mensagem", motivo));
        } catch (Exception e) {
            log.error("Falha ao publicar erro saga.reply", e);
        }
    }

    private void publicar(Map<String, Object> reply) {
        try {
            rabbitTemplate.convertAndSend(EXCHANGE, REPLY, objectMapper.writeValueAsString(reply));
        } catch (Exception e) {
            log.error("Falha ao publicar saga.reply", e);
        }
    }
}
