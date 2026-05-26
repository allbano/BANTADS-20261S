package br.dac.bantads.ms_cliente.application.dto;

import java.math.BigDecimal;

public record ClienteResponse(
        String cpf,
        String nome,
        String email,
        String telefone,
        String endereco,
        String cidade,
        String estado,
        String conta,
        BigDecimal saldo,
        BigDecimal limite
) {}
