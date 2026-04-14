package br.dac.bantads.ms_auth.application.dto;

public record ResetTestBaseResponse(
        int keptBaseAccounts,
        int removedAccounts
) {
}
