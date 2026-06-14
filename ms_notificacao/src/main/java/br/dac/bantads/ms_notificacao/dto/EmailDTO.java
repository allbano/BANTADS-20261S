package br.dac.bantads.ms_notificacao.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Contrato GENÉRICO de e-mail do BANTADS: qualquer microsserviço que precise
 * enviar um e-mail (por qualquer motivo) publica este envelope na fila
 * {@code FILA_EMAIL}; o ms_notificacao é o único que efetivamente fala com o SMTP.
 *
 * Formato fixo: {@code destino} (e-mail de destino), {@code assunto} e
 * {@code mensagem} (corpo já pronto pelo produtor).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailDTO {
    private String destino;
    private String assunto;
    private String mensagem;
}
