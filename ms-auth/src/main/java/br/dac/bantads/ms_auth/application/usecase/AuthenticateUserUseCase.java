package br.dac.bantads.ms_auth.application.usecase;

import br.dac.bantads.ms_auth.application.dto.AuthRequest;
import br.dac.bantads.ms_auth.application.dto.AuthResponse;
import br.dac.bantads.ms_auth.application.exception.InvalidCredentialsException;
import br.dac.bantads.ms_auth.application.security.JwtTokenService;
import br.dac.bantads.ms_auth.application.security.PasswordHasher;
import br.dac.bantads.ms_auth.domain.account.UserAccountRepository;
import br.dac.bantads.ms_auth.domain.account.UserAccount;
import org.springframework.stereotype.Service;

@Service
public class AuthenticateUserUseCase {
    private final UserAccountRepository userAccountRepository;
    private final PasswordHasher passwordHasher;
    private final JwtTokenService jwtTokenService;

    public AuthenticateUserUseCase(
            UserAccountRepository userAccountRepository,
            PasswordHasher passwordHasher,
            JwtTokenService jwtTokenService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.passwordHasher = passwordHasher;
        this.jwtTokenService = jwtTokenService;
    }

    public AuthResponse execute(AuthRequest request) {
        UserAccount account = userAccountRepository
                .findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordHasher.matches(request.password(), account.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtTokenService.generate(account);
        return new AuthResponse(
                "Bearer",
                token,
                account.getEmail(),
                account.getAccountRole().authority()
        );
    }
}
