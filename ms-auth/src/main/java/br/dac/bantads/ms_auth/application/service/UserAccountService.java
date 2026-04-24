package br.dac.bantads.ms_auth.application.service;

import br.dac.bantads.ms_auth.application.dto.AccountResponse;
import br.dac.bantads.ms_auth.application.dto.CreateAccountResponse;
import br.dac.bantads.ms_auth.application.dto.CreateAccountWithPasswordRequest;
import br.dac.bantads.ms_auth.application.dto.CreateAccountWithoutPasswordRequest;
import br.dac.bantads.ms_auth.application.dto.UpdateAccountRequest;
import br.dac.bantads.ms_auth.application.exception.AccountNotFoundException;
import br.dac.bantads.ms_auth.application.exception.EmailNotificationException;
import br.dac.bantads.ms_auth.application.security.PasswordHasher;
import br.dac.bantads.ms_auth.domain.account.UserAccountRepository;
import br.dac.bantads.ms_auth.domain.account.AccountRole;
import br.dac.bantads.ms_auth.domain.account.UserAccount;
import br.dac.bantads.ms_auth.application.event.RandomPasswordGeneratedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Set;

@Service
public class UserAccountService {
    private static final int GENERATED_PASSWORD_SIZE = 8;
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String NUMBERS = "0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserAccountRepository userAccountRepository;
    private final PasswordHasher passwordHasher;
    private final ApplicationEventPublisher eventPublisher;

    public UserAccountService(UserAccountRepository userAccountRepository, PasswordHasher passwordHasher,
            ApplicationEventPublisher eventPublisher) {
        this.userAccountRepository = userAccountRepository;
        this.passwordHasher = passwordHasher;
        this.eventPublisher = eventPublisher;
    }

    public CreateAccountResponse createWithPassword(CreateAccountWithPasswordRequest request) {
        UserAccount account = new UserAccount(
                request.email(),
                passwordHasher.hash(request.password()),
                request.accountRole());
        UserAccount saved = userAccountRepository.save(account);
        return new CreateAccountResponse(saved.getEmail(), saved.getAccountRole().name(), null);
    }

    public CreateAccountResponse createWithoutPassword(CreateAccountWithoutPasswordRequest request) {
        String generatedPassword = generateRandomPassword();
        UserAccount account = new UserAccount(
                request.email(),
                passwordHasher.hash(generatedPassword),
                request.accountRole());
        UserAccount saved = userAccountRepository.save(account);
        try {
            eventPublisher.publishEvent(new RandomPasswordGeneratedEvent(saved.getEmail(), generatedPassword));
        } catch (Exception ex) {
            userAccountRepository.deleteByEmail(saved.getEmail()); // Reverte a criação do usuário se falhar o e-mail
            throw new EmailNotificationException(
                    "Falha ao enviar e-mail com a senha. A criação do usuário foi revertida.", ex);
        }
        return new CreateAccountResponse(saved.getEmail(), saved.getAccountRole().name(), null); // Hiding the password
                                                                                                 // from the response
    }

    public UserAccount createOrUpdateWithPassword(String email, String rawPassword, AccountRole accountRole) {
        UserAccount account = new UserAccount(
                email,
                passwordHasher.hash(rawPassword),
                accountRole);
        return userAccountRepository.save(account);
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

    public String generateRandomPasswordHash() {
        return passwordHasher.hash(generateRandomPassword());
    }

    private String generateRandomPassword() {
        String characters = LETTERS + NUMBERS;
        StringBuilder sb = new StringBuilder(GENERATED_PASSWORD_SIZE);
        for (int i = 0; i < GENERATED_PASSWORD_SIZE; i++) {
            int index = RANDOM.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }
}
