package br.dac.bantads.ms_saga.orchestrator;

import br.dac.bantads.ms_saga.config.RabbitMQConfig;
import br.dac.bantads.ms_saga.dto.AutocadastroRequestDTO;
import br.dac.bantads.ms_saga.saga.SagaInstance;
import br.dac.bantads.ms_saga.saga.SagaStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutocadastroSagaOrchestrator {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    // Estado em memória — suficiente para o contexto acadêmico
    private final Map<UUID, SagaInstance> sagaStore = new ConcurrentHashMap<>();

    // ---------------------------------------------------------------
    // Entrada: API chama este método para iniciar o fluxo de autocadastro
    // ---------------------------------------------------------------
    public UUID iniciarSaga(AutocadastroRequestDTO req) {
        UUID sagaId = UUID.randomUUID();
        SagaInstance saga = new SagaInstance(
                sagaId, req.nome(), req.email(), req.senha(), req.cpf(),
                req.telefone(), req.salario(), req.endereco(), req.cep(),
                req.cidade(), req.estado()
        );
        sagaStore.put(sagaId, saga);
        log.info("SAGA {} iniciada para o cliente '{}'", sagaId, req.email());
        enviarPassoRegistrarCliente(saga);
        return sagaId;
    }

    // ---------------------------------------------------------------
    // Passo 1 → ms-cliente registra o cliente
    // ---------------------------------------------------------------
    private void enviarPassoRegistrarCliente(SagaInstance saga) {
        saga.setStatus(SagaStatus.AGUARDANDO_CLIENTE);
        Map<String, Object> dto = new HashMap<>();
        dto.put("nome",     saga.getNome());
        dto.put("email",    saga.getEmail());
        dto.put("senha",    saga.getSenha());
        dto.put("cpf",      saga.getCpf());
        dto.put("telefone", saga.getTelefone());
        dto.put("salario",  saga.getSalario());
        dto.put("endereco", saga.getEndereco());
        dto.put("cep",      saga.getCep());
        dto.put("cidade",   saga.getCidade());
        dto.put("estado",   saga.getEstado());
        dto.put("cargo",    "CLIENTE");
        dto.put("ativo",    false);
        dto.put("sagaId",   saga.getSagaId().toString());
        publicar(RabbitMQConfig.FILA_REGISTRO_CLIENTE, dto, saga.getSagaId());
    }

    // ---------------------------------------------------------------
    // Callback: ms-cliente criou o cliente com sucesso
    // ---------------------------------------------------------------
    public void onClienteCriado(UUID sagaId, String uuidCliente) {
        SagaInstance saga = sagaStore.get(sagaId);
        if (saga == null) { log.warn("SAGA {} não encontrada (onClienteCriado)", sagaId); return; }
        saga.setUuidCliente(UUID.fromString(uuidCliente));
        saga.setStatus(SagaStatus.CLIENTE_CRIADO);
        log.info("SAGA {}: cliente {} criado — avançando para conta", sagaId, uuidCliente);
        enviarPassoRegistrarConta(saga);
    }

    // ---------------------------------------------------------------
    // Passo 2 → ms-conta cria a conta bancária
    // ---------------------------------------------------------------
    private void enviarPassoRegistrarConta(SagaInstance saga) {
        saga.setStatus(SagaStatus.AGUARDANDO_CONTA);
        Map<String, Object> dto = new HashMap<>();
        dto.put("uuidCliente", saga.getUuidCliente().toString());
        dto.put("salario",     saga.getSalario());
        dto.put("ativo",       false);
        dto.put("sagaId",      saga.getSagaId().toString());
        publicar(RabbitMQConfig.FILA_REGISTRO_CONTA_CLIENTE, dto, saga.getSagaId());
    }

    // ---------------------------------------------------------------
    // Callback: ms-conta criou a conta com sucesso
    // ---------------------------------------------------------------
    public void onContaCriada(UUID sagaId) {
        SagaInstance saga = sagaStore.get(sagaId);
        if (saga == null) { log.warn("SAGA {} não encontrada (onContaCriada)", sagaId); return; }
        saga.setStatus(SagaStatus.CONTA_CRIADA);
        log.info("SAGA {}: conta criada — avançando para autenticação", sagaId);
        enviarPassoRegistrarAuth(saga);
    }

    // ---------------------------------------------------------------
    // Passo 3 → ms-auth cria as credenciais
    // ---------------------------------------------------------------
    private void enviarPassoRegistrarAuth(SagaInstance saga) {
        saga.setStatus(SagaStatus.AGUARDANDO_AUTH);
        Map<String, Object> dto = new HashMap<>();
        dto.put("_id",    saga.getUuidCliente().toString());
        dto.put("email",  saga.getEmail());
        dto.put("senha",  saga.getSenha());
        dto.put("cargo",  "CLIENTE");
        dto.put("ativo",  false);
        dto.put("sagaId", saga.getSagaId().toString());
        publicar(RabbitMQConfig.FILA_AUTENTICACAO, dto, saga.getSagaId());
    }

    // ---------------------------------------------------------------
    // Callback: ms-auth criou as credenciais — saga concluída!
    // ---------------------------------------------------------------
    public void onAuthCriado(UUID sagaId) {
        SagaInstance saga = sagaStore.get(sagaId);
        if (saga == null) { log.warn("SAGA {} não encontrada (onAuthCriado)", sagaId); return; }
        saga.setStatus(SagaStatus.CONCLUIDA);
        log.info("SAGA {}: CONCLUÍDA com sucesso para '{}'", sagaId, saga.getEmail());
        sagaStore.remove(sagaId);
    }

    // ---------------------------------------------------------------
    // Compensação: desfaz os passos já executados
    // ---------------------------------------------------------------
    public void onErro(UUID sagaId, String motivo) {
        SagaInstance saga = sagaStore.get(sagaId);
        if (saga == null) { log.warn("SAGA {} não encontrada (onErro)", sagaId); return; }

        SagaStatus statusAnterior = saga.getStatus();
        saga.setStatus(SagaStatus.COMPENSANDO);
        log.warn("SAGA {}: compensando a partir do status '{}'. Motivo: {}", sagaId, statusAnterior, motivo);

        // Compensa conta se já foi criada
        if (saga.getUuidCliente() != null &&
                (statusAnterior == SagaStatus.CONTA_CRIADA || statusAnterior == SagaStatus.AGUARDANDO_AUTH)) {
            enviarCompensacaoConta(saga);
        }

        // Compensa cliente se já foi criado
        if (saga.getUuidCliente() != null) {
            enviarCompensacaoCliente(saga);
        }

        saga.setStatus(SagaStatus.FALHOU);
        log.error("SAGA {}: FALHOU. Cliente: '{}'", sagaId, saga.getEmail());
    }

    private void enviarCompensacaoConta(SagaInstance saga) {
        try {
            String uuidJson = objectMapper.writeValueAsString(saga.getUuidCliente().toString());
            rabbitTemplate.convertAndSend(RabbitMQConfig.FILA_ERRO_NOVO_CLIENTE, uuidJson);
            log.info("SAGA {}: compensação de conta enviada", saga.getSagaId());
        } catch (JsonProcessingException e) {
            log.error("SAGA {}: falha ao serializar compensação de conta", saga.getSagaId(), e);
        }
    }

    private void enviarCompensacaoCliente(SagaInstance saga) {
        try {
            String uuidJson = objectMapper.writeValueAsString(saga.getUuidCliente().toString());
            rabbitTemplate.convertAndSend(RabbitMQConfig.SAGA_CMD_EXCLUIR_CLIENTE, uuidJson);
            log.info("SAGA {}: compensação de cliente enviada", saga.getSagaId());
        } catch (JsonProcessingException e) {
            log.error("SAGA {}: falha ao serializar compensação de cliente", saga.getSagaId(), e);
        }
    }

    // ---------------------------------------------------------------
    // Consulta de status (para o endpoint HTTP de monitoramento)
    // ---------------------------------------------------------------
    public SagaStatus consultarStatus(UUID sagaId) {
        SagaInstance saga = sagaStore.get(sagaId);
        return saga != null ? saga.getStatus() : null;
    }

    // ---------------------------------------------------------------
    // Utilitário de publicação
    // ---------------------------------------------------------------
    private void publicar(String fila, Map<String, Object> payload, UUID sagaId) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            rabbitTemplate.convertAndSend(fila, json);
            log.debug("SAGA {}: mensagem publicada em '{}'", sagaId, fila);
        } catch (JsonProcessingException e) {
            log.error("SAGA {}: erro ao serializar para a fila '{}'", sagaId, fila, e);
            onErro(sagaId, "Erro de serialização: " + e.getMessage());
        }
    }
}
