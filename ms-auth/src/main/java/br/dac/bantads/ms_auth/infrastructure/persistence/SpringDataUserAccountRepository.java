package br.dac.bantads.ms_auth.infrastructure.persistence;

import br.dac.bantads.ms_auth.domain.account.UserAccount;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.Set;

public interface SpringDataUserAccountRepository extends MongoRepository<UserAccount, String> {

    Optional<UserAccount> findByEmail(String email);

    void deleteByEmail(String email);

    int deleteAllByEmailNotIn(Set<String> emails);
}
