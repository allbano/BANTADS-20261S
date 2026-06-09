package br.dac.bantads.ms_conta.model.event;

import br.dac.bantads.ms_conta.model.ContaModel;

/**
 * Evento de domínio publicado após qualquer alteração em ContaModel.
 * O ContaEventHandler consome este evento e atualiza o modelo de leitura (ContaView).
 */
public record ContaAtualizadaEvent(ContaModel conta) {}
