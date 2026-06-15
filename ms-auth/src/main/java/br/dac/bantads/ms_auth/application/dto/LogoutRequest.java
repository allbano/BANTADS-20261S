package br.dac.bantads.ms_auth.application.dto;

/**
 * Corpo (opcional) do logout. O "login" corresponde ao e-mail do usuario que
 * esta saindo, usado para montar o LogoutResponse.
 */
public record LogoutRequest(String login) {
}
