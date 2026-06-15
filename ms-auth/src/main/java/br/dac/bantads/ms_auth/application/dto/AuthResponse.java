package br.dac.bantads.ms_auth.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Resposta do login (contrato oficial LoginResponse):
 * { access_token, token_type, tipo: [CLIENTE|GERENTE|ADMIN], usuario }.
 *
 * O ms-auth e responsavel apenas pela autenticacao, entao preenche apenas o
 * e-mail em `usuario`. Os campos `nome` e `cpf` do contrato oficial sao
 * compostos pelo api-gateway (API Composition com ms-cliente/ms-funcionario).
 */
public record AuthResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        String tipo,
        UsuarioDTO usuario
) {
    public record UsuarioDTO(String email) {}
}
