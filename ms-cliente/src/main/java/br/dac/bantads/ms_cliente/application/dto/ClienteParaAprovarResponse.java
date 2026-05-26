package br.dac.bantads.ms_cliente.application.dto;

import java.math.BigDecimal;

public record ClienteParaAprovarResponse(
        String cpf,
        String nome,
        String email,
        BigDecimal salario,
        String endereco,
        String cidade,
        String estado
) {}
