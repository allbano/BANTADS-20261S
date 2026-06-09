package br.dac.bantads.ms_saga.saga;

public enum SagaStatus {
    INICIADA,
    AGUARDANDO_CLIENTE,
    CLIENTE_CRIADO,
    AGUARDANDO_CONTA,
    CONTA_CRIADA,
    AGUARDANDO_AUTH,
    CONCLUIDA,
    COMPENSANDO,
    FALHOU
}
