package br.dac.bantads.ms_cliente.dto;

import java.util.UUID;
import java.math.BigDecimal;

public record ClienteResponseDTO(
        UUID uuid,
        String nome,
        String email,
        String cpf,
        BigDecimal salario,
        EnderecoDTO endereco
) {}