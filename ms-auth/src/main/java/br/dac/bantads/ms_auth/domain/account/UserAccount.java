package br.dac.bantads.ms_auth.domain.account;


public class UserAccount {
    private final String email;
    private final String passwordHash;
    private final AccountRole accountRole;

    public UserAccount(String email, String passwordHash, AccountRole accountRole) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.accountRole = accountRole;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public AccountRole getAccountRole() {
        return accountRole;
    }

    public boolean passwordMatches(String hash) {
        return this.passwordHash != null && this.passwordHash.equals(hash);
    }
}
