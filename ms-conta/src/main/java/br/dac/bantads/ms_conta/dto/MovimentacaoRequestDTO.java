package br.dac.bantads.ms_conta.dto;

import java.math.BigDecimal;
import java.util.UUID;

import br.dac.bantads.ms_conta.model.enums.TipoMovimentacao;

/**
 * DTO de requisição para operações bancárias.
 *
 * Campos:
 * - tipo: DEPOSITO (R5), SAQUE (R6) ou TRANSFERENCIA (R7)
 * - valor: montante da operação (sempre positivo)
 * - uuidContaDestino: obrigatório apenas para TRANSFERENCIA (R7),
 *   informando a conta corrente destino
 */
public record MovimentacaoRequestDTO(
        TipoMovimentacao tipo,
        BigDecimal valor,
        UUID uuidContaDestino
) {}
