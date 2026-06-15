package br.dac.bantads.ms_auth.application.dto;

/**
 * Resposta do logout (contrato oficial LogoutResponse):
 * { cpf, nome, email, tipo }.
 */
public record LogoutResponse(
        String cpf,
        String nome,
        String email,
        String tipo
) {
}
