package br.dac.bantads.ms_auth.application.dto;

public record CreateAccountResponse(
        String email,
        String accountRole,
        String generatedPassword
) {
}
