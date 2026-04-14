package br.dac.bantads.ms_auth.domain.account;

public enum AccountRole {
    CLIENTE,
    GERENTE,
    ADMINISTRADOR;

    public static AccountRole fromValue(String value) {
        for (AccountRole role : values()) {
            if (role.name().equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Role invalida: " + value);
    }

    public String authority() {
        return name().toLowerCase();
    }
}
