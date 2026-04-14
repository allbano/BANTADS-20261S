package br.dac.bantads.ms_auth.application.service;

import br.dac.bantads.ms_auth.application.dto.ResetTestBaseResponse;
import br.dac.bantads.ms_auth.domain.account.AccountRole;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
public class BaseTestAccountSeedService {
    private static final String DEFAULT_TEST_PASSWORD = "tads";
    private static final Map<String, AccountRole> BASE_ACCOUNTS = Map.ofEntries(
            Map.entry("cli1@bantads.com.br", AccountRole.CLIENTE),
            Map.entry("cli2@bantads.com.br", AccountRole.CLIENTE),
            Map.entry("cli3@bantads.com.br", AccountRole.CLIENTE),
            Map.entry("cli4@bantads.com.br", AccountRole.CLIENTE),
            Map.entry("cli5@bantads.com.br", AccountRole.CLIENTE),
            Map.entry("ger1@bantads.com.br", AccountRole.GERENTE),
            Map.entry("ger2@bantads.com.br", AccountRole.GERENTE),
            Map.entry("ger3@bantads.com.br", AccountRole.GERENTE),
            Map.entry("adm1@bantads.com.br", AccountRole.ADMINISTRADOR)
    );

    private final UserAccountService userAccountService;

    public BaseTestAccountSeedService(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    public void ensureBaseTestAccounts() {
        BASE_ACCOUNTS.forEach(
                (email, role) -> userAccountService.createOrUpdateWithPassword(email, DEFAULT_TEST_PASSWORD, role)
        );
    }

    public ResetTestBaseResponse rebootBase() {
        ensureBaseTestAccounts();
        int removed = userAccountService.deleteAccountsNotIn(baseEmails());
        return new ResetTestBaseResponse(BASE_ACCOUNTS.size(), removed);
    }

    public Set<String> baseEmails() {
        return BASE_ACCOUNTS.keySet();
    }
}
