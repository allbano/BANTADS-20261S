package br.dac.bantads.ms_conta.consumer;

import br.dac.bantads.ms_conta.config.RabbitMQConfig;
import br.dac.bantads.ms_conta.dto.ContaRabbitDTO;
import br.dac.bantads.ms_conta.model.cud.ContaModel;
import br.dac.bantads.ms_conta.service.ContaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQConsumer {

    private final ContaService contaService;
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.FILA_REGISTRO_CONTA_CLIENTE)
    public void registraNovoCliente(String msg) {
        log.info("Mensagem recebida na fila {}: {}", RabbitMQConfig.FILA_REGISTRO_CONTA_CLIENTE, msg);
        String sagaId = null;
        try {
            ContaRabbitDTO dto = objectMapper.readValue(msg, ContaRabbitDTO.class);
            sagaId = dto.getSagaId();
            ContaModel conta = contaService.registrarConta(dto);
            log.info("Conta registrada com sucesso para o cliente: {}. UUID da Conta: {}", conta.getUuidCliente(), conta.getUuidConta());
            publicarSagaContaCriada(sagaId, conta.getUuidCliente().toString());
        } catch (Exception e) {
            log.error("Erro ao registrar novo cliente a partir da fila: {}", e.getMessage(), e);
            publicarSagaContaErro(sagaId, e.getMessage());
        }
    }

    private void publicarSagaContaCriada(String sagaId, String uuidCliente) {
        if (sagaId == null || sagaId.isBlank()) return;
        try {
            String json = String.format(
                    "{\"sagaId\":\"%s\",\"sucesso\":true,\"uuidCliente\":\"%s\"}", sagaId, uuidCliente);
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.SAGA_EVT_CONTA_CRIADA, json);
        } catch (Exception e) {
            log.error("Falha ao publicar SAGA_EVT_CONTA_CRIADA", e);
        }
    }

    private void publicarSagaContaErro(String sagaId, String motivo) {
        if (sagaId == null || sagaId.isBlank()) return;
        try {
            String m = motivo != null ? motivo.replace("\"", "'") : "erro no ms-conta";
            String json = String.format(
                    "{\"sagaId\":\"%s\",\"sucesso\":false,\"mensagem\":\"%s\"}", sagaId, m);
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.SAGA_EVT_CONTA_ERRO, json);
        } catch (Exception e) {
            log.error("Falha ao publicar SAGA_EVT_CONTA_ERRO", e);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.FILA_ERRO_NOVO_CLIENTE)
    public void erroCriacaoClienteRollback(String msg) {
        log.info("Mensagem recebida na fila {}: {}", RabbitMQConfig.FILA_ERRO_NOVO_CLIENTE, msg);
        try {
            String clientUuidStr;
            try {
                clientUuidStr = objectMapper.readValue(msg, String.class);
            } catch (Exception e) {
                clientUuidStr = msg.replace("\"", "").trim();
            }

            if (clientUuidStr != null && !clientUuidStr.isBlank()) {
                UUID clientUuid = UUID.fromString(clientUuidStr);
                contaService.excluirContaPorCliente(clientUuid);
                log.info("Rollback executado: conta excluída para o cliente {}", clientUuid);
            } else {
                log.warn("Nenhum UUID de cliente identificado na mensagem de erro");
            }
        } catch (Exception e) {
            log.error("Erro ao processar rollback de criação de cliente: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.FILA_ATRIBUI_CONTA_GERENTE)
    public void atribuiContaGerente(String msg) {
        log.info("Mensagem recebida na fila {}: {}", RabbitMQConfig.FILA_ATRIBUI_CONTA_GERENTE, msg);
        try {
            String gerenteUuidStr;
            try {
                gerenteUuidStr = objectMapper.readValue(msg, String.class);
            } catch (Exception e) {
                gerenteUuidStr = msg.replace("\"", "").trim();
            }

            if (gerenteUuidStr != null && !gerenteUuidStr.isBlank()) {
                UUID gerenteUuid = UUID.fromString(gerenteUuidStr);
                contaService.atribuiContaGerente(gerenteUuid);
            } else {
                log.warn("Nenhum UUID de gerente identificado na mensagem");
            }
        } catch (Exception e) {
            log.error("Erro ao processar atribuição de gerente: {}", e.getMessage(), e);
        }
    }

}
