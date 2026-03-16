package br.dac.bantads.ms_funcionario.dto;

import br.dac.bantads.ms_funcionario.dto.enums.TipoFuncionario;

public record FuncionarioResponseDTO(
        String cpf,
        String nome,
        String email,
        TipoFuncionario tipo
) {}