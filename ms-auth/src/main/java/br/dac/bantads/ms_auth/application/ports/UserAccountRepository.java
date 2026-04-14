package br.dac.bantads.ms_auth.application.ports;

import br.dac.bantads.ms_auth.domain.account.UserAccount;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserAccountRepository {
    Optional<UserAccount> findByEmail(String email);

    UserAccount save(UserAccount userAccount);

    List<UserAccount> findAll();

    int deleteAllByEmailNotIn(Set<String> emails);

    void deleteByEmail(String email);
}
