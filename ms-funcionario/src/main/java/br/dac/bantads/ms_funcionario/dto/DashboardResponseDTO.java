package br.dac.bantads.ms_funcionario.dto;

import br.dac.bantads.ms_funcionario.dto.external.SaldoResponseDTO;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DashboardResponseDTO(
        FuncionarioResponseDTO gerente,
        List<SaldoResponseDTO> clientes,
        @JsonProperty("saldo_positivo") Float saldoPositivo,
        @JsonProperty("saldo_negativo") Float saldoNegativo
) {}