package br.dac.bantads.ms_auth.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        String tipo,
        UsuarioDTO usuario
) {
    public record UsuarioDTO(String email) {}
}
