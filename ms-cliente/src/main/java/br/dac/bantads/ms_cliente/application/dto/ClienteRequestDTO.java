package br.dac.bantads.ms_cliente.application.dto;

import java.math.BigDecimal;

public record ClienteRequestDTO(
        String nome,
        String email,
        String cpf,
        String telefone,
        BigDecimal salario,
        String endereco,
        String cep,
        String cidade,
        String estado,
        String senha,
        String cargo,
        Boolean ativo
) {}
