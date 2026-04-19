package br.dac.bantads.ms_auth.domain.account;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user_accounts")
public class UserAccount {
    @Id
    private final String email;
    private final String passwordHash;
    private final AccountRole accountRole;

    @PersistenceCreator
    public UserAccount(String email, String passwordHash, AccountRole accountRole) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email nao pode ser nulo ou vazio");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("Password hash nao pode ser nulo ou vazio");
        }
        if (accountRole == null) {
            throw new IllegalArgumentException("Account role nao pode ser nulo");
        }
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
}
