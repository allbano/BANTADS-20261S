package br.dac.bantads.ms_auth.application.dto;

public record UpdateAccountRequest(
        String password,
        String accountRole
) {
}
