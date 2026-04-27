package br.dac.bantads.ms_conta.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO de resposta com os dados da conta bancária.
 * Utilizado na tela inicial do cliente (R3), consulta de cliente (R13),
 * e demais operações que exibem informações da conta.
 */
public record ContaResponseDTO(
        UUID uuidConta,
        UUID uuidCliente,
        String numero,
        LocalDate dataCriacao,
        BigDecimal saldo,
        BigDecimal limite,
        UUID uuidGerente
) {}
