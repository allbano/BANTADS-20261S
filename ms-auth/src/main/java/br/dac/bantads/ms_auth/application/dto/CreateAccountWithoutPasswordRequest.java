package br.dac.bantads.ms_auth.application.dto;

import br.dac.bantads.ms_auth.domain.account.AccountRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateAccountWithoutPasswordRequest(
        @Email @NotBlank String email,
        @NotNull AccountRole accountRole
) {
}
