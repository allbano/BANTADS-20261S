package br.dac.bantads.ms_auth.domain.account;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserAccountRepository {

    Optional<UserAccount> findByEmail(String email);

    UserAccount save(UserAccount userAccount);

    List<UserAccount> findAll();

    void deleteByEmail(String email);

    int deleteAllByEmailNotIn(Set<String> emails);
}
