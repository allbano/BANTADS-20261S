package br.dac.bantads.ms_auth.interfaces.rest;

import br.dac.bantads.ms_auth.application.dto.AuthRequest;
import br.dac.bantads.ms_auth.application.dto.AuthResponse;
import br.dac.bantads.ms_auth.application.dto.CreateAccountResponse;
import br.dac.bantads.ms_auth.application.dto.CreateAccountWithPasswordRequest;
import br.dac.bantads.ms_auth.application.dto.CreateAccountWithoutPasswordRequest;
import br.dac.bantads.ms_auth.application.dto.ResetTestBaseResponse;
import br.dac.bantads.ms_auth.application.service.BaseTestAccountSeedService;
import br.dac.bantads.ms_auth.application.service.UserAccountService;
import br.dac.bantads.ms_auth.application.usecase.AuthenticateUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Auth", description = "Endpoints de autenticacao e gestao de contas")
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

    @PostMapping
    @Operation(summary = "Autentica usuario e gera token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticacao realizada"),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciais invalidas",
                    content = @Content(schema = @Schema(implementation = java.util.Map.class))
            )
    })
    public ResponseEntity<AuthResponse> authenticate(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authenticateUserUseCase.execute(request));
    }

    @PostMapping("/accounts/with-password")
    @Operation(summary = "Cria conta informando senha em texto puro")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Conta criada"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos")
    })
    public ResponseEntity<CreateAccountResponse> createWithPassword(
            @Valid @RequestBody CreateAccountWithPasswordRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userAccountService.createWithPassword(request));
    }

    @PostMapping("/accounts/without-password")
    @Operation(summary = "Cria conta com senha aleatoria de 6 letras")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Conta criada com senha gerada"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos")
    })
    public ResponseEntity<CreateAccountResponse> createWithoutPassword(
            @Valid @RequestBody CreateAccountWithoutPasswordRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userAccountService.createWithoutPassword(request));
    }

    @PostMapping("/reboot")
    @Operation(summary = "Reinicia base de usuarios de teste e remove contas extras")
    @ApiResponse(responseCode = "200", description = "Base reiniciada")
    public ResponseEntity<ResetTestBaseResponse> reboot() {
        return ResponseEntity.ok(baseTestAccountSeedService.rebootBase());
    }

    @GetMapping("/accounts")
    @Operation(summary = "Lista todas as contas cadastradas")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        return ResponseEntity.ok(userAccountService.getAllAccounts());
    }

    @GetMapping("/accounts/{email}")
    @Operation(summary = "Busca uma conta por email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta encontrada"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    public ResponseEntity<AccountResponse> getAccountByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userAccountService.getAccountByEmail(email));
    }

    @PutMapping("/accounts/{email}")
    @Operation(summary = "Atualiza uma conta existente (senha ou cargo)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta atualizada"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    public ResponseEntity<AccountResponse> updateAccount(
            @PathVariable String email,
            @RequestBody UpdateAccountRequest request
    ) {
        return ResponseEntity.ok(userAccountService.updateAccount(email, request));
    }

    @DeleteMapping("/accounts/{email}")
    @Operation(summary = "Apaga uma conta por email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Conta removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    public ResponseEntity<Void> deleteAccount(@PathVariable String email) {
        userAccountService.deleteAccountByEmail(email);
        return ResponseEntity.noContent().build();
    }
}
