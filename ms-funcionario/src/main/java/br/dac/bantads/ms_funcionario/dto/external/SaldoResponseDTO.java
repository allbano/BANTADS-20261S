package br.dac.bantads.ms_funcionario.dto.external;

public record SaldoResponseDTO(
     String cliente,
     String conta,
     Double saldo
) {}
