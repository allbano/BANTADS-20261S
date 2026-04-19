package br.dac.bantads.ms_auth.infrastructure.persistence;

import br.dac.bantads.ms_auth.domain.account.UserAccount;
import br.dac.bantads.ms_auth.domain.account.UserAccountRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class UserAccountRepositoryImpl implements UserAccountRepository {

    private final SpringDataUserAccountRepository springDataRepository;

    public UserAccountRepositoryImpl(SpringDataUserAccountRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Optional<UserAccount> findByEmail(String email) {
        return springDataRepository.findByEmail(email);
    }

    @Override
    public UserAccount save(UserAccount userAccount) {
        return springDataRepository.save(userAccount);
    }

    @Override
    public List<UserAccount> findAll() {
        return springDataRepository.findAll();
    }

    @Override
    public void deleteByEmail(String email) {
        springDataRepository.deleteByEmail(email);
    }

    @Override
    public int deleteAllByEmailNotIn(Set<String> emails) {
        return springDataRepository.deleteAllByEmailNotIn(emails);
    }
}
