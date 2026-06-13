package br.dac.bantads.ms_auth.interfaces.rest;

import br.dac.bantads.ms_auth.application.dto.AuthRequest;
import br.dac.bantads.ms_auth.application.dto.AuthResponse;
import br.dac.bantads.ms_auth.application.dto.CreateAccountResponse;
import br.dac.bantads.ms_auth.application.dto.CreateAccountWithPasswordRequest;
import br.dac.bantads.ms_auth.application.dto.CreateAccountWithoutPasswordRequest;
import br.dac.bantads.ms_auth.application.dto.LogoutRequest;
import br.dac.bantads.ms_auth.application.dto.LogoutResponse;
import br.dac.bantads.ms_auth.application.dto.ResetTestBaseResponse;
import br.dac.bantads.ms_auth.application.service.BaseTestAccountSeedService;
import br.dac.bantads.ms_auth.application.service.UserAccountService;
import br.dac.bantads.ms_auth.application.usecase.AuthenticateUserUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import br.dac.bantads.ms_auth.application.dto.AccountResponse;
import br.dac.bantads.ms_auth.application.dto.UpdateAccountRequest;


@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final UserAccountService userAccountService;
    private final BaseTestAccountSeedService baseTestAccountSeedService;

    public AuthController(
            AuthenticateUserUseCase authenticateUserUseCase,
            UserAccountService userAccountService,
            BaseTestAccountSeedService baseTestAccountSeedService
    ) {
        this.authenticateUserUseCase = authenticateUserUseCase;
        this.userAccountService = userAccountService;
        this.baseTestAccountSeedService = baseTestAccountSeedService;
    }

    // R2 - Login: autentica (login/senha) e gera token JWT
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticate(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authenticateUserUseCase.execute(request));
    }

    // R2 - Logout: JWT stateless; retorna os dados de autenticacao do usuario que saiu.
    // O descarte do token e responsabilidade do cliente; cpf/nome sao compostos pelo gateway.
    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(@RequestBody(required = false) LogoutRequest request) {
        String login = request != null ? request.login() : null;
        return ResponseEntity.ok(userAccountService.logout(login));
    }

    @PostMapping("/accounts/with-password")
    public ResponseEntity<CreateAccountResponse> createWithPassword(
            @Valid @RequestBody CreateAccountWithPasswordRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userAccountService.createWithPassword(request));
    }

    @PostMapping("/accounts/without-password")
    public ResponseEntity<CreateAccountResponse> createWithoutPassword(
            @Valid @RequestBody CreateAccountWithoutPasswordRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userAccountService.createWithoutPassword(request));
    }

    // Reboot: reinicia a base de credenciais ao estado do seed
    @PostMapping("/reboot")
    public ResponseEntity<ResetTestBaseResponse> reboot() {
        return ResponseEntity.ok(baseTestAccountSeedService.rebootBase());
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        return ResponseEntity.ok(userAccountService.getAllAccounts());
    }

    @GetMapping("/accounts/{email}")
    public ResponseEntity<AccountResponse> getAccountByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userAccountService.getAccountByEmail(email));
    }

    @PutMapping("/accounts/{email}")
    public ResponseEntity<AccountResponse> updateAccount(
            @PathVariable String email,
            @RequestBody UpdateAccountRequest request
    ) {
        return ResponseEntity.ok(userAccountService.updateAccount(email, request));
    }

    @DeleteMapping("/accounts/{email}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String email) {
        userAccountService.deleteAccountByEmail(email);
        return ResponseEntity.noContent().build();
    }
}
