package br.dac.bantads.ms_auth.application.security;

import br.dac.bantads.ms_auth.domain.account.UserAccount;

public interface JwtTokenService {
    String generate(UserAccount userAccount);
}
