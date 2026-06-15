package br.dac.bantads.ms_auth.application.service;

import br.dac.bantads.ms_auth.application.dto.AccountResponse;
import br.dac.bantads.ms_auth.application.dto.CreateAccountResponse;
import br.dac.bantads.ms_auth.application.dto.CreateAccountWithPasswordRequest;
import br.dac.bantads.ms_auth.application.dto.LogoutResponse;
import br.dac.bantads.ms_auth.application.dto.UpdateAccountRequest;
import br.dac.bantads.ms_auth.application.exception.AccountNotFoundException;
import br.dac.bantads.ms_auth.application.security.PasswordHasher;
import br.dac.bantads.ms_auth.domain.account.UserAccountRepository;
import br.dac.bantads.ms_auth.domain.account.AccountRole;
import br.dac.bantads.ms_auth.domain.account.UserAccount;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordHasher passwordHasher;

    public UserAccountService(UserAccountRepository userAccountRepository, PasswordHasher passwordHasher) {
        this.userAccountRepository = userAccountRepository;
        this.passwordHasher = passwordHasher;
    }

    public CreateAccountResponse createWithPassword(CreateAccountWithPasswordRequest request) {
        UserAccount account = new UserAccount(
                request.email(),
                passwordHasher.hash(request.password()),
                request.accountRole());
        UserAccount saved = userAccountRepository.save(account);
        return new CreateAccountResponse(saved.getEmail(), saved.getAccountRole().name(), null);
    }

    public UserAccount createOrUpdateWithPassword(String email, String rawPassword, AccountRole accountRole) {
        UserAccount account = new UserAccount(
                email,
                passwordHasher.hash(rawPassword),
                accountRole);
        return userAccountRepository.save(account);
    }

    /**
     * Monta o LogoutResponse { cpf, nome, email, tipo } a partir do login (e-mail).
     * O ms-auth so conhece email e tipo; cpf/nome sao compostos pelo api-gateway
     * (API Composition). Como o JWT e stateless, o logout apenas devolve os dados
     * de autenticacao do usuario que saiu; o descarte do token e do cliente.
     */
    public LogoutResponse logout(String login) {
        if (login == null || login.isBlank()) {
            return new LogoutResponse(null, null, login, null);
        }
        return userAccountRepository.findByEmail(login)
                .map(acc -> new LogoutResponse(null, null, acc.getEmail(), acc.getAccountRole().tipoApi()))
                .orElse(new LogoutResponse(null, null, login, null));
    }

    public int deleteAccountsNotIn(Set<String> emails) {
        return userAccountRepository.deleteAllByEmailNotIn(emails);
    }

    public List<AccountResponse> getAllAccounts() {
        return userAccountRepository.findAll().stream()
                .map(acc -> new AccountResponse(acc.getEmail(), acc.getAccountRole().name()))
                .toList();
    }

    public AccountResponse getAccountByEmail(String email) {
        UserAccount account = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new AccountNotFoundException("Account with email " + email + " not found"));
        return new AccountResponse(account.getEmail(), account.getAccountRole().name());
    }

    public AccountResponse updateAccount(String email, UpdateAccountRequest request) {
        UserAccount existing = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new AccountNotFoundException("Account with email " + email + " not found"));

        String updatedPassword = existing.getPasswordHash();
        if (request.password() != null && !request.password().isBlank()) {
            updatedPassword = passwordHasher.hash(request.password());
        }

        AccountRole updatedRole = existing.getAccountRole();
        if (request.accountRole() != null) {
            updatedRole = AccountRole.fromValue(request.accountRole());
        }

        UserAccount updatedAccount = new UserAccount(existing.getEmail(), updatedPassword, updatedRole);
        userAccountRepository.save(updatedAccount);

        return new AccountResponse(updatedAccount.getEmail(), updatedAccount.getAccountRole().name());
    }

    public void deleteAccountByEmail(String email) {
        userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new AccountNotFoundException("Account with email " + email + " not found"));
        userAccountRepository.deleteByEmail(email);
    }
}
