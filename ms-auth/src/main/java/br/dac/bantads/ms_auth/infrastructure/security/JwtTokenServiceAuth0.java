package br.dac.bantads.ms_auth.infrastructure.security;

import br.dac.bantads.ms_auth.application.security.JwtTokenService;
import br.dac.bantads.ms_auth.domain.account.UserAccount;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class JwtTokenServiceAuth0 implements JwtTokenService {
    private final JwtProperties jwtProperties;

    public JwtTokenServiceAuth0(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public String generate(UserAccount userAccount) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(jwtProperties.expirationMinutes(), ChronoUnit.MINUTES);

        return JWT.create()
                .withIssuer(jwtProperties.issuer())
                .withSubject(userAccount.getEmail())
                .withIssuedAt(now)
                .withExpiresAt(expiresAt)
                .withClaim("email", userAccount.getEmail())
                .withClaim("role", userAccount.getAccountRole().authority())
                .sign(Algorithm.HMAC256(jwtProperties.secret()));
    }
}
