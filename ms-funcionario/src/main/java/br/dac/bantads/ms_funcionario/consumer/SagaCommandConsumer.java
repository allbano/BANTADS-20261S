package br.dac.bantads.ms_funcionario.consumer;

import br.dac.bantads.ms_funcionario.domain.FuncionarioModel;
import br.dac.bantads.ms_funcionario.dto.enums.TipoFuncionario;
import br.dac.bantads.ms_funcionario.service.FuncionarioService;
import br.dac.bantads.ms_funcionario.utils.Security;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Comandos das SAGAs genéricas (Eixo 3) endereçados ao ms-funcionario: inserir
 * gerente (R17), validar/excluir gerente (R18) e alterar gerente (R20).
 * Responde sempre em {@code saga.reply}.
 */
@Component
@Slf4j
public class SagaCommandConsumer {

    private static final String EXCHANGE = "bantads.topic";
    private static final String REPLY = "saga.reply";

    @Autowired private FuncionarioService service;
    @Autowired private RabbitTemplate rabbitTemplate;
    @Autowired private ObjectMapper objectMapper;

    // ── R17: inserir gerente ──
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "saga.cmd.gerente.inserir", durable = "true"),
            exchange = @Exchange(value = EXCHANGE, type = "topic"),
            key = "saga.cmd.gerente.inserir"))
    public void inserir(String msg) {
        try {
            JsonNode n = objectMapper.readTree(msg);
            String sagaId = n.path("sagaId").asText();
            String cpf = n.path("cpf").asText();
            String email = n.path("email").asText();

            if (service.getByCpf(cpf).isPresent()) { replyErro(sagaId, "CPF já cadastrado"); return; }
            if (service.getByEmail(email).isPresent()) { replyErro(sagaId, "Email já cadastrado"); return; }

            FuncionarioModel g = FuncionarioModel.builder()
                    .cpf(cpf)
                    .nome(n.path("nome").asText())
                    .email(email)
                    .telefone(n.path("telefone").asText(null))
                    .senha(Security.hash(n.path("senha").asText("")))
                    .tipo(TipoFuncionario.GERENTE)
                    .build();
            FuncionarioModel salvo = service.saveFuncionario(g);
            replyOk(sagaId, Map.of("uuidGerente", salvo.getUuid().toString(), "email", salvo.getEmail()));
        } catch (Exception e) {
            replyErroSafe(msg, "Falha ao inserir gerente: " + e.getMessage());
        }
    }

    // ── R18 passo 1: validar remoção (existe, é gerente e não é o último) ──
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "saga.cmd.gerente.validar.remocao", durable = "true"),
            exchange = @Exchange(value = EXCHANGE, type = "topic"),
            key = "saga.cmd.gerente.validar.remocao"))
    public void validarRemocao(String msg) {
        try {
            JsonNode n = objectMapper.readTree(msg);
            String sagaId = n.path("sagaId").asText();
            Optional<FuncionarioModel> opt = service.getByCpf(n.path("cpf").asText());
            if (opt.isEmpty() || opt.get().getTipo() != TipoFuncionario.GERENTE) {
                replyErro(sagaId, "Gerente não encontrado"); return;
            }
            long totalGerentes = service.listByTipo(TipoFuncionario.GERENTE).size();
            if (totalGerentes <= 1) {
                replyErro(sagaId, "Não é possível remover o último gerente"); return;
            }
            FuncionarioModel g = opt.get();
            replyOk(sagaId, Map.of("uuidGerente", g.getUuid().toString(), "email", g.getEmail()));
        } catch (Exception e) {
            replyErroSafe(msg, "Falha ao validar remoção de gerente: " + e.getMessage());
        }
    }

    // ── R18 passo 3 / compensação do R17: excluir gerente ──
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "saga.cmd.gerente.excluir", durable = "true"),
            exchange = @Exchange(value = EXCHANGE, type = "topic"),
            key = "saga.cmd.gerente.excluir"))
    public void excluir(String msg) {
        try {
            JsonNode n = objectMapper.readTree(msg);
            String sagaId = n.path("sagaId").asText();
            UUID uuid = UUID.fromString(n.path("uuidGerente").asText());
            try {
                service.delete(uuid);
            } catch (IllegalArgumentException ignored) {
                log.warn("Gerente {} já inexistente ao excluir", uuid);
            }
            replyOk(sagaId, Map.of());
        } catch (Exception e) {
            replyErroSafe(msg, "Falha ao excluir gerente: " + e.getMessage());
        }
    }

    // ── R20: alterar gerente (nome/email) ──
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "saga.cmd.gerente.alterar", durable = "true"),
            exchange = @Exchange(value = EXCHANGE, type = "topic"),
            key = "saga.cmd.gerente.alterar"))
    public void alterar(String msg) {
        try {
            JsonNode n = objectMapper.readTree(msg);
            String sagaId = n.path("sagaId").asText();
            Optional<FuncionarioModel> opt = service.getByCpf(n.path("cpf").asText());
            if (opt.isEmpty()) { replyErro(sagaId, "Gerente não encontrado"); return; }

            FuncionarioModel g = opt.get();
            if (n.hasNonNull("nome"))  g.setNome(n.get("nome").asText());
            if (n.hasNonNull("email")) g.setEmail(n.get("email").asText());
            if (n.hasNonNull("telefone")) g.setTelefone(n.get("telefone").asText());
            FuncionarioModel salvo = service.saveFuncionario(g);
            replyOk(sagaId, Map.of("uuidGerente", salvo.getUuid().toString(), "email", salvo.getEmail()));
        } catch (Exception e) {
            replyErroSafe(msg, "Falha ao alterar gerente: " + e.getMessage());
        }
    }

    private void replyOk(String sagaId, Map<String, Object> dados) {
        publicar(Map.of("sagaId", sagaId, "sucesso", true, "dados", dados));
    }

    private void replyErro(String sagaId, String motivo) {
        publicar(Map.of("sagaId", sagaId, "sucesso", false, "mensagem", motivo));
    }

    private void replyErroSafe(String originalMsg, String motivo) {
        try {
            replyErro(objectMapper.readTree(originalMsg).path("sagaId").asText(), motivo);
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
