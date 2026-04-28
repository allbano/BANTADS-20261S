package br.dac.bantads.ms_conta.dto;

import java.time.LocalDate;

/**
 * DTO de requisição para consulta de extrato (R8).
 * O cliente informa a data de início e a data de fim para filtrar
 * as movimentações da sua conta.
 */
public record ExtratoRequestDTO(
        LocalDate dataInicio,
        LocalDate dataFim
) {}
