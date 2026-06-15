package br.dac.bantads.ms_cliente.infrastructure.messaging;

import br.dac.bantads.ms_cliente.domain.model.ClienteModel;
import br.dac.bantads.ms_cliente.domain.repository.ClienteRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Comandos das SAGAs genéricas (Eixo 3) endereçados ao ms-cliente: aprovar (R10)
 * e atualizar perfil (R4), com as respectivas compensações. Cada handler executa
 * a escrita local e responde no canal único {@code saga.reply}.
 */
@Component
@Slf4j
public class SagaCommandConsumer {

    private static final String EXCHANGE = "bantads.topic";
    private static final String REPLY = "saga.reply";

    private final ClienteRepository repository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    public SagaCommandConsumer(ClienteRepository repository, RabbitTemplate rabbitTemplate) {
        this.repository = repository;
        this.rabbitTemplate = rabbitTemplate;
    }

    // ── R10: aprovar cliente ──
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "saga.cmd.cliente.aprovar", durable = "true"),
            exchange = @Exchange(value = EXCHANGE, type = "topic"),
            key = "saga.cmd.cliente.aprovar"))
    public void aprovar(String msg) {
        try {
            JsonNode n = objectMapper.readTree(msg);
            String sagaId = n.path("sagaId").asText();
            Optional<ClienteModel> opt = repository.findByCpf(n.path("cpf").asText());
            if (opt.isEmpty()) { replyErro(sagaId, "Cliente não encontrado para aprovação"); return; }

            ClienteModel c = opt.get();
            c.setAtivo(true);
            c.setStatus("APROVADO"); // R10 — sai da fila de "aguardando aprovação"
            repository.save(c);

            Map<String, Object> dados = new HashMap<>();
            dados.put("uuidCliente", c.getUuid().toString());
            dados.put("email", c.getEmail());
            dados.put("nome", c.getNome());
            dados.put("salario", c.getSalario());
            dados.put("telefone", c.getTelefone());
            replyOk(sagaId, dados);
        } catch (Exception e) {
            log.error("Erro em saga.cmd.cliente.aprovar: {}", e.getMessage(), e);
        }
    }

    // ── compensação do aprovar ──
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "saga.cmd.cliente.reprovar", durable = "true"),
            exchange = @Exchange(value = EXCHANGE, type = "topic"),
            key = "saga.cmd.cliente.reprovar"))
    public void reprovar(String msg) {
        try {
            JsonNode n = objectMapper.readTree(msg);
            repository.findByCpf(n.path("cpf").asText()).ifPresent(c -> {
                c.setAtivo(false);
                c.setStatus("PENDENTE"); // compensação: volta para "aguardando aprovação"
                repository.save(c);
                log.info("Compensação: cliente {} reprovado (status=PENDENTE)", c.getCpf());
            });
            replyOk(n.path("sagaId").asText(), Map.of());
        } catch (Exception e) {
            log.error("Erro em saga.cmd.cliente.reprovar: {}", e.getMessage(), e);
        }
    }

    // ── R4: atualizar perfil (CPF imutável) ──
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "saga.cmd.cliente.atualizar", durable = "true"),
            exchange = @Exchange(value = EXCHANGE, type = "topic"),
            key = "saga.cmd.cliente.atualizar"))
    public void atualizar(String msg) {
        try {
            JsonNode n = objectMapper.readTree(msg);
            String sagaId = n.path("sagaId").asText();
            Optional<ClienteModel> opt = repository.findByCpf(n.path("cpf").asText());
            if (opt.isEmpty()) { replyErro(sagaId, "Cliente não encontrado para alteração"); return; }

            ClienteModel c = opt.get();
            Map<String, Object> dados = new HashMap<>();
            // snapshot antigo para compensação
            dados.put("oldNome", c.getNome());
            dados.put("oldEmail", c.getEmail());
            dados.put("oldTelefone", c.getTelefone());
            dados.put("oldSalario", c.getSalario());
            dados.put("oldEndereco", c.getEndereco());
            dados.put("oldCep", c.getCep());
            dados.put("oldCidade", c.getCidade());
            dados.put("oldEstado", c.getEstado());

            if (n.hasNonNull("nome"))     c.setNome(n.get("nome").asText());
            if (n.hasNonNull("email"))    c.setEmail(n.get("email").asText());
            if (n.hasNonNull("telefone")) c.setTelefone(n.get("telefone").asText());
            if (n.hasNonNull("salario"))  c.setSalario(n.get("salario").decimalValue());
            if (n.hasNonNull("endereco")) c.setEndereco(n.get("endereco").asText());
            if (n.hasNonNull("cep"))      c.setCep(n.get("cep").asText());
            if (n.hasNonNull("cidade"))   c.setCidade(n.get("cidade").asText());
            if (n.hasNonNull("estado"))   c.setEstado(n.get("estado").asText());
            repository.save(c);

            dados.put("uuidCliente", c.getUuid().toString());
            dados.put("salario", c.getSalario());
            replyOk(sagaId, dados);
        } catch (Exception e) {
            log.error("Erro em saga.cmd.cliente.atualizar: {}", e.getMessage(), e);
        }
    }

    // ── compensação do atualizar ──
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "saga.cmd.cliente.restaurar", durable = "true"),
            exchange = @Exchange(value = EXCHANGE, type = "topic"),
            key = "saga.cmd.cliente.restaurar"))
    public void restaurar(String msg) {
        try {
            JsonNode n = objectMapper.readTree(msg);
            repository.findByCpf(n.path("cpf").asText()).ifPresent(c -> {
                if (n.hasNonNull("oldNome"))     c.setNome(n.get("oldNome").asText());
                if (n.hasNonNull("oldEmail"))    c.setEmail(n.get("oldEmail").asText());
                if (n.hasNonNull("oldTelefone")) c.setTelefone(n.get("oldTelefone").asText());
                if (n.hasNonNull("oldSalario"))  c.setSalario(n.get("oldSalario").decimalValue());
                if (n.hasNonNull("oldEndereco")) c.setEndereco(n.get("oldEndereco").asText());
                if (n.hasNonNull("oldCep"))      c.setCep(n.get("oldCep").asText());
                if (n.hasNonNull("oldCidade"))   c.setCidade(n.get("oldCidade").asText());
                if (n.hasNonNull("oldEstado"))   c.setEstado(n.get("oldEstado").asText());
                repository.save(c);
                log.info("Compensação: perfil do cliente {} restaurado", c.getCpf());
            });
            replyOk(n.path("sagaId").asText(), Map.of());
        } catch (Exception e) {
            log.error("Erro em saga.cmd.cliente.restaurar: {}", e.getMessage(), e);
        }
    }

    private void replyOk(String sagaId, Map<String, Object> dados) {
        publicar(Map.of("sagaId", sagaId, "sucesso", true, "dados", dados));
    }

    private void replyErro(String sagaId, String motivo) {
        publicar(Map.of("sagaId", sagaId, "sucesso", false, "mensagem", motivo));
    }

    private void publicar(Map<String, Object> reply) {
        try {
            rabbitTemplate.convertAndSend(EXCHANGE, REPLY, objectMapper.writeValueAsString(reply));
        } catch (Exception e) {
            log.error("Falha ao publicar saga.reply", e);
        }
    }
}
