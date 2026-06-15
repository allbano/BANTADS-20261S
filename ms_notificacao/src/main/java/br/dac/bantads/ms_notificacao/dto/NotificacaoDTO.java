package br.dac.bantads.ms_notificacao.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Contrato da mensagem que trafega na fila {@code FILA_NOTIFICACAO}.
 *
 * E o "envelope" que os outros microsservicos publicam quando precisam que um
 * e-mail seja enviado. O ms_notificacao apenas le esta mensagem e dispara o
 * e-mail para o {@code email} informado.
 *
 * Exemplos de payload JSON:
 * <pre>
 * { "tipo": "APROVACAO",          "email": "cli1@bantads.com.br", "nome": "Joao", "senha": "AB12CD" }
 * { "tipo": "REJEICAO",           "email": "cli1@bantads.com.br", "nome": "Joao", "motivo": "Renda insuficiente" }
 * { "tipo": "FALHA_AUTOCADASTRO", "email": "cli1@bantads.com.br", "nome": "Joao" }
 * </pre>
 *
 * {@code @JsonIgnoreProperties(ignoreUnknown = true)} torna o parsing tolerante:
 * se um publicador enviar campos extras, a desserializacao nao quebra.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificacaoDTO {

    /** Discrimina qual e-mail montar (APROVACAO, REJEICAO, FALHA_AUTOCADASTRO). */
    private TipoNotificacao tipo;

    /** Destinatario do e-mail. Campo obrigatorio em qualquer tipo. */
    private String email;

    /** Nome do destinatario, usado na saudacao do e-mail (opcional). */
    private String nome;

    /** Senha de acesso gerada na aprovacao. Preenchido apenas em {@code APROVACAO}. */
    private String senha;

    /** Motivo da recusa. Preenchido apenas em {@code REJEICAO}. */
    private String motivo;
}
