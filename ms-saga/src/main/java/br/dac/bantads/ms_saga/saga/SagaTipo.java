package br.dac.bantads.ms_saga.saga;

/**
 * Tipos de SAGA orquestrada do BANTADS (Eixo 3). O AUTOCADASTRO (R1) permanece
 * com seu orquestrador dedicado; estes são os fluxos do mecanismo genérico.
 */
public enum SagaTipo {
    APROVAR_CLIENTE,   // R10
    ALTERAR_PERFIL,    // R4
    INSERIR_GERENTE,   // R17
    REMOVER_GERENTE,   // R18
    ALTERAR_GERENTE    // R20
}
