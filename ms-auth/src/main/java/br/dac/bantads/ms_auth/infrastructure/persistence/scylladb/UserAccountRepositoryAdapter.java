package br.dac.bantads.ms_auth.infrastructure.persistence.scylladb;

import br.dac.bantads.ms_auth.application.ports.UserAccountRepository;
import br.dac.bantads.ms_auth.domain.account.AccountRole;
import br.dac.bantads.ms_auth.domain.account.UserAccount;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class UserAccountRepositoryAdapter implements UserAccountRepository {
    private final SpringDataUserAccountRepository repository;

    public UserAccountRepositoryAdapter(SpringDataUserAccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<UserAccount> findByEmail(String email) {
        return repository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public UserAccount save(UserAccount userAccount) {
        UserAccountRow saved = repository.save(toRow(userAccount));
        return toDomain(saved);
    }

    @Override
    public List<UserAccount> findAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public int deleteAllByEmailNotIn(Set<String> emails) {
        List<UserAccountRow> toDelete = repository.findAll().stream()
                .filter(row -> !emails.contains(row.getEmail()))
                .toList();
        repository.deleteAll(toDelete);
        return toDelete.size();
    }

    private UserAccount toDomain(UserAccountRow row) {
        return new UserAccount(
                row.getEmail(),
                row.getPasswordHash(),
                AccountRole.fromValue(row.getAccountRole())
        );
    }

    private UserAccountRow toRow(UserAccount userAccount) {
        UserAccountRow row = new UserAccountRow();
        row.setEmail(userAccount.getEmail());
        row.setPasswordHash(userAccount.getPasswordHash());
        row.setAccountRole(userAccount.getAccountRole().name());
        return row;
    }

    @Override
    public void deleteByEmail(String email) {
        repository.deleteById(email);
    }
}
