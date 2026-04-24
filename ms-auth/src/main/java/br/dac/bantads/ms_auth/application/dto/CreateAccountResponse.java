package br.dac.bantads.ms_auth.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateAccountResponse(
        String email,
        String accountRole,
        String generatedPassword
) {
}
