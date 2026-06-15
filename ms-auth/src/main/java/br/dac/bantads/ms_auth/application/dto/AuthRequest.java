package br.dac.bantads.ms_auth.application.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Corpo da requisicao de login (contrato oficial: campos "login" e "senha").
 * O "login" corresponde ao e-mail do usuario.
 */
public record AuthRequest(
        @NotBlank String login,
        @NotBlank String senha
) {
}
