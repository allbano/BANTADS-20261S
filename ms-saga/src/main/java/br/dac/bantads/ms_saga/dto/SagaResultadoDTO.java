package br.dac.bantads.ms_saga.dto;

import java.util.Map;

/**
 * Resultado final de uma SAGA executada em modo SÍNCRONO (bloqueante).
 * O gateway interpreta {@code sucesso}/{@code dados} para montar a resposta no
 * formato do contrato test_dac e o status code apropriado.
 */
public record SagaResultadoDTO(
        String sagaId,
        String tipo,
        String status,
        boolean sucesso,
        String erro,
        Map<String, Object> dados
) {}
