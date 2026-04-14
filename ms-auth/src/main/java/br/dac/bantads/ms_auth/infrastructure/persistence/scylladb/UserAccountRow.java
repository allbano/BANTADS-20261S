package br.dac.bantads.ms_auth.infrastructure.persistence.scylladb;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("user_accounts")
public class UserAccountRow {
    @PrimaryKey
    @Column("email")
    private String email;

    @Column("password_hash")
    private String passwordHash;

    @Column("account_role")
    private String accountRole;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getAccountRole() {
        return accountRole;
    }

    public void setAccountRole(String accountRole) {
        this.accountRole = accountRole;
    }
}
