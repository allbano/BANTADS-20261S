package br.dac.bantads.ms_notificacao.dto;

/**
 * Tipos de notificacao que o ms_notificacao sabe enviar por e-mail.
 *
 * Cada valor corresponde a um momento de negocio das SAGAs:
 * <ul>
 *   <li>{@link #CONTA_CRIADA}      - R1: autocadastro recebido; confirma que a solicitacao foi enviada.</li>
 *   <li>{@link #APROVACAO}         - R10: cliente aprovado; e-mail leva a senha gerada.</li>
 *   <li>{@link #REJEICAO}          - R11: cliente rejeitado; e-mail leva o motivo.</li>
 *   <li>{@link #FALHA_AUTOCADASTRO}- R1: autocadastro nao efetuado (falha/compensacao da SAGA).</li>
 * </ul>
 *
 * Centralizacao: o produtor (ms-auth / ms-saga / ms-cliente) apenas define o
 * tipo na mensagem tipada publicada em {@code saga.cmd.notificar.cliente}; SOMENTE
 * o ms_notificacao monta assunto/corpo e fala com o SMTP.
 */
public enum TipoNotificacao {
    CONTA_CRIADA,
    APROVACAO,
    REJEICAO,
    FALHA_AUTOCADASTRO
}
