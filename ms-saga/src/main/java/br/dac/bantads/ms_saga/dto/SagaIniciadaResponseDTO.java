package br.dac.bantads.ms_saga.dto;

import java.util.UUID;

public record SagaIniciadaResponseDTO(UUID sagaId, String status) {}
