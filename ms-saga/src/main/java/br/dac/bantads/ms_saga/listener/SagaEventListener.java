package br.dac.bantads.ms_saga.listener;

import br.dac.bantads.ms_saga.config.RabbitMQConfig;
import br.dac.bantads.ms_saga.dto.SagaRespostaDTO;
import br.dac.bantads.ms_saga.orchestrator.AutocadastroSagaOrchestrator;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Escuta os eventos de resposta publicados pelos serviços participantes
 * e delega ao orquestrador para avançar ou compensar a saga.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SagaEventListener {

    private final AutocadastroSagaOrchestrator orchestrator;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitMQConfig.SAGA_EVT_CLIENTE_CRIADO)
    void onClienteCriado(String msg) {
        log.debug("Recebido SAGA_EVT_CLIENTE_CRIADO: {}", msg);
        try {
            SagaRespostaDTO resp = objectMapper.readValue(msg, SagaRespostaDTO.class);
            if (resp.isSucesso()) {
                orchestrator.onClienteCriado(resp.getSagaId(), resp.getUuidCliente());
            } else {
                orchestrator.onErro(resp.getSagaId(), resp.getMensagem());
            }
        } catch (Exception e) {
            log.error("Erro ao processar SAGA_EVT_CLIENTE_CRIADO", e);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.SAGA_EVT_CLIENTE_ERRO)
    void onClienteErro(String msg) {
        log.debug("Recebido SAGA_EVT_CLIENTE_ERRO: {}", msg);
        processarErro(msg, "falha no ms-cliente");
    }

    @RabbitListener(queues = RabbitMQConfig.SAGA_EVT_CONTA_CRIADA)
    void onContaCriada(String msg) {
        log.debug("Recebido SAGA_EVT_CONTA_CRIADA: {}", msg);
        try {
            SagaRespostaDTO resp = objectMapper.readValue(msg, SagaRespostaDTO.class);
            if (resp.isSucesso()) {
                orchestrator.onContaCriada(resp.getSagaId());
            } else {
                orchestrator.onErro(resp.getSagaId(), resp.getMensagem());
            }
        } catch (Exception e) {
            log.error("Erro ao processar SAGA_EVT_CONTA_CRIADA", e);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.SAGA_EVT_CONTA_ERRO)
    void onContaErro(String msg) {
        log.debug("Recebido SAGA_EVT_CONTA_ERRO: {}", msg);
        processarErro(msg, "falha no ms-conta");
    }

    @RabbitListener(queues = RabbitMQConfig.SAGA_EVT_AUTH_CRIADO)
    void onAuthCriado(String msg) {
        log.debug("Recebido SAGA_EVT_AUTH_CRIADO: {}", msg);
        try {
            SagaRespostaDTO resp = objectMapper.readValue(msg, SagaRespostaDTO.class);
            if (resp.isSucesso()) {
                orchestrator.onAuthCriado(resp.getSagaId());
            } else {
                orchestrator.onErro(resp.getSagaId(), resp.getMensagem());
            }
        } catch (Exception e) {
            log.error("Erro ao processar SAGA_EVT_AUTH_CRIADO", e);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.SAGA_EVT_AUTH_ERRO)
    void onAuthErro(String msg) {
        log.debug("Recebido SAGA_EVT_AUTH_ERRO: {}", msg);
        processarErro(msg, "falha no ms-auth");
    }

    private void processarErro(String msg, String fallbackMotivo) {
        try {
            SagaRespostaDTO resp = objectMapper.readValue(msg, SagaRespostaDTO.class);
            String motivo = resp.getMensagem() != null ? resp.getMensagem() : fallbackMotivo;
            orchestrator.onErro(resp.getSagaId(), motivo);
        } catch (Exception e) {
            log.error("Erro ao processar evento de falha da saga. Mensagem raw: {}", msg, e);
        }
    }
}
