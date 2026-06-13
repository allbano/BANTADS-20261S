package br.dac.bantads.ms_conta.service;

import br.dac.bantads.ms_conta.config.RabbitMQConfig;
import br.dac.bantads.ms_conta.dto.ContaSyncDTO;
import br.dac.bantads.ms_conta.model.cud.ContaModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Publicador do eixo CQRS do MS Conta.
 *
 * Apos cada escrita no banco de Comando (conta_cud), o lado Comando publica um
 * evento no Topic Exchange {@code bantads.topic}. O {@code ContaCqrsConsumer}
 * (lado Consulta) recebe e atualiza a projecao no banco de leitura (conta_r).
 * Assim os dois bancos fisicos sao sincronizados exclusivamente por mensageria.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CqrsPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    /** Publica o snapshot atual da conta para projecao na ContaView. */
    public void publicarConta(ContaModel conta) {
        ContaSyncDTO dto = new ContaSyncDTO(
                conta.getUuidConta(),
                conta.getUuidCliente(),
                conta.getNumero(),
                conta.getDataCriacao(),
                conta.getSaldo(),
                conta.getLimite(),
                conta.getUuidGerente(),
                conta.isAtivo(),
                conta.getRejeitadoMotivo(),
                conta.getRejeitadoData()
        );
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_CONTA_ATUALIZADA,
                    objectMapper.writeValueAsString(dto));
            log.debug("CQRS sync publicado (atualizada) para conta {}", conta.getUuidConta());
        } catch (Exception e) {
            log.error("Falha ao serializar/publicar CQRS sync da conta {}", conta.getUuidConta(), e);
        }
    }

    /** Publica a exclusao de uma conta (por UUID de cliente) para remover da ContaView. */
    public void publicarExclusao(UUID uuidCliente) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.RK_CONTA_EXCLUIDA, uuidCliente.toString());
        log.debug("CQRS sync publicado (excluida) para cliente {}", uuidCliente);
    }
}
