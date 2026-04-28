package br.dac.bantads.ms_conta.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import br.dac.bantads.ms_conta.model.enums.TipoMovimentacao;

/**
 * DTO de resposta para cada movimentação no extrato (R8).
 *
 * Campos apresentados conforme requisito:
 * - dataHora: data/hora da transação
 * - tipo: operação (DEPOSITO, SAQUE, TRANSFERENCIA)
 * - uuidContaOrigem/uuidContaDestino: preenchidos em caso de transferência
 * - valor: montante da operação
 *
 * Regras de exibição no front-end:
 * - Saída (SAQUE, TRANSFERENCIA enviada) → vermelho
 * - Entrada (DEPOSITO, TRANSFERENCIA recebida) → azul
 */
public record MovimentacaoResponseDTO(
        UUID uuidMovimentacao,
        LocalDateTime dataHora,
        TipoMovimentacao tipo,
        UUID uuidContaOrigem,
        UUID uuidContaDestino,
        BigDecimal valor
) {}
