package br.dac.bantads.ms_auth.application.dto;

public record AuthResponse(
        String tokenType,
        String token,
        String email,
        String accountRole
) {
}
