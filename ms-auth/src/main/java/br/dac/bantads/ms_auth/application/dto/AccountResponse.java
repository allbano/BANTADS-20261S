package br.dac.bantads.ms_auth.application.dto;

public record AccountResponse(
        String email,
        String accountRole
) {
}
