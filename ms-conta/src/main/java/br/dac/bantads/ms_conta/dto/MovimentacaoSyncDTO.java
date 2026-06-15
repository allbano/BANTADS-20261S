package br.dac.bantads.ms_conta.dto;

import br.dac.bantads.ms_conta.model.enums.TipoMovimentacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payload de sincronização CQRS das movimentações (lado Comando -> RabbitMQ -> Consulta).
 *
 * Carrega o snapshot denormalizado da movimentação para projeção na MovimentacaoView
 * (banco de leitura conta_r). Inclui os números de conta (própria e destino) para
 * dispensar qualquer lookup no read side. Não trafega entidade JPA — apenas este DTO.
 */
public record MovimentacaoSyncDTO(
        UUID uuidMovimentacao,
        UUID uuidConta,
        String numeroConta,
        LocalDateTime dataHora,
        TipoMovimentacao tipo,
        BigDecimal valor,
        UUID uuidContaDestino,
        String numeroContaDestino
) {}
