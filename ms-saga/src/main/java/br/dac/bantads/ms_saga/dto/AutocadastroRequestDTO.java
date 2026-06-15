package br.dac.bantads.ms_saga.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.math.BigDecimal;

public record AutocadastroRequestDTO(
        String nome,
        String email,
        String senha,
        String cpf,
        String telefone,
        BigDecimal salario,
        String endereco,
        @JsonAlias("CEP") String cep,
        String cidade,
        String estado
) {}
