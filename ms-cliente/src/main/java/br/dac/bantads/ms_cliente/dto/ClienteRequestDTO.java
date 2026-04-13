package br.dac.bantads.ms_cliente.dto;

import java.math.BigDecimal;

public record ClienteRequestDTO(
        String nome,
        String email,
        String cpf,
        String telefone,
        BigDecimal salario,
        EnderecoDTO endereco
) {}