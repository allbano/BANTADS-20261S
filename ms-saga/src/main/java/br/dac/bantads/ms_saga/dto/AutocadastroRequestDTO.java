package br.dac.bantads.ms_saga.dto;

import java.math.BigDecimal;

public record AutocadastroRequestDTO(
        String nome,
        String email,
        String senha,
        String cpf,
        String telefone,
        BigDecimal salario,
        String endereco,
        String cep,
        String cidade,
        String estado
) {}
