package br.dac.bantads.ms_funcionario.dto.external;

import java.time.OffsetDateTime;

public record ContaResponseDTO (
        String cliente,
        String numero,
        Double saldo,
        Double limite,
        String gerente,
        OffsetDateTime criacao
) {}
