package br.dac.bantads.ms_notificacao.dto;

/**
 * Tipos de notificacao que o ms_notificacao sabe enviar por e-mail.
 *
 * Cada valor corresponde a um momento de negocio das SAGAs:
 * <ul>
 *   <li>{@link #APROVACAO}         - RF10: cliente aprovado; e-mail leva a senha gerada.</li>
 *   <li>{@link #REJEICAO}          - RF11: cliente rejeitado; e-mail leva o motivo.</li>
 *   <li>{@link #FALHA_AUTOCADASTRO}- RF01: autocadastro nao efetuado (compensacao da SAGA).</li>
 * </ul>
 *
 * O publicador (ms-auth / ms-saga / ms-cliente) define o tipo na mensagem; o
 * consumidor usa o tipo para montar assunto e corpo do e-mail corretos.
 */
public enum TipoNotificacao {
    APROVACAO,
    REJEICAO,
    FALHA_AUTOCADASTRO
}
