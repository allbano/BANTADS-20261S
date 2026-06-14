package br.dac.bantads.ms_saga.controller;

import br.dac.bantads.ms_saga.dto.AutocadastroRequestDTO;
import br.dac.bantads.ms_saga.dto.SagaIniciadaResponseDTO;
import br.dac.bantads.ms_saga.orchestrator.AutocadastroSagaOrchestrator;
import br.dac.bantads.ms_saga.orchestrator.GenericSagaOrchestrator;
import br.dac.bantads.ms_saga.saga.GenericSagaContext;
import br.dac.bantads.ms_saga.saga.SagaStatus;
import br.dac.bantads.ms_saga.saga.SagaTipo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/saga")
@CrossOrigin
@RequiredArgsConstructor
public class SagaController {

    private final AutocadastroSagaOrchestrator orchestrator;
    private final GenericSagaOrchestrator generic;

    /**
     * Inicia a saga de autocadastro de um novo cliente.
     * O orquestrador coordena ms-cliente → ms-conta → ms-auth com compensação automática.
     */
    @PostMapping("/autocadastro")
    ResponseEntity<SagaIniciadaResponseDTO> iniciarAutocadastro(
            @RequestBody AutocadastroRequestDTO request) {
        UUID sagaId = orchestrator.iniciarSaga(request);
        return ResponseEntity.accepted()
                .body(new SagaIniciadaResponseDTO(sagaId, "INICIADA"));
    }

    /**
     * Consulta o status atual de uma saga em andamento.
     */
    @GetMapping("/autocadastro/{sagaId}")
    ResponseEntity<Map<String, String>> consultarStatus(@PathVariable UUID sagaId) {
        SagaStatus status = orchestrator.consultarStatus(sagaId);
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("sagaId", sagaId.toString(), "status", status.name()));
    }

    // ───────────────────────── SAGAs genéricas (Eixo 3) ─────────────────────────

    /** R10 — Aprovar cliente: cliente → conta → auth (senha aleatória) → notificação. */
    @PostMapping("/clientes/{cpf}/aprovar")
    ResponseEntity<SagaIniciadaResponseDTO> aprovarCliente(@PathVariable String cpf,
                                                           @RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> dados = new HashMap<>(body != null ? body : Map.of());
        dados.put("cpf", cpf);
        return aceitar(generic.iniciar(SagaTipo.APROVAR_CLIENTE, dados));
    }

    /** R4 — Alteração de perfil: cliente (CPF imutável) → conta (recalcula limite). */
    @PutMapping("/clientes/{cpf}")
    ResponseEntity<SagaIniciadaResponseDTO> alterarPerfil(@PathVariable String cpf,
                                                          @RequestBody Map<String, Object> body) {
        Map<String, Object> dados = new HashMap<>(body != null ? body : Map.of());
        dados.put("cpf", cpf);
        return aceitar(generic.iniciar(SagaTipo.ALTERAR_PERFIL, dados));
    }

    /** R17 — Inserção de gerente: gerente → auth → conta (transfere 1 conta ao novo). */
    @PostMapping("/gerentes")
    ResponseEntity<SagaIniciadaResponseDTO> inserirGerente(@RequestBody Map<String, Object> body) {
        return aceitar(generic.iniciar(SagaTipo.INSERIR_GERENTE, new HashMap<>(body != null ? body : Map.of())));
    }

    /** R18 — Remoção de gerente: valida (não-último) → conta (redistribui) → gerente → auth. */
    @DeleteMapping("/gerentes/{cpf}")
    ResponseEntity<SagaIniciadaResponseDTO> removerGerente(@PathVariable String cpf,
                                                           @RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> dados = new HashMap<>(body != null ? body : Map.of());
        dados.put("cpf", cpf);
        return aceitar(generic.iniciar(SagaTipo.REMOVER_GERENTE, dados));
    }

    /** R20 — Alteração de gerente: gerente (nome/email) → auth (senha, se enviada). */
    @PutMapping("/gerentes/{cpf}")
    ResponseEntity<SagaIniciadaResponseDTO> alterarGerente(@PathVariable String cpf,
                                                           @RequestBody Map<String, Object> body) {
        Map<String, Object> dados = new HashMap<>(body != null ? body : Map.of());
        dados.put("cpf", cpf);
        return aceitar(generic.iniciar(SagaTipo.ALTERAR_GERENTE, dados));
    }

    /** Consulta de status de uma SAGA genérica. */
    @GetMapping("/status/{sagaId}")
    ResponseEntity<Map<String, String>> consultarGenerica(@PathVariable UUID sagaId) {
        GenericSagaContext ctx = generic.consultar(sagaId);
        if (ctx == null) {
            return ResponseEntity.notFound().build();
        }
        Map<String, String> resp = new HashMap<>();
        resp.put("sagaId", sagaId.toString());
        resp.put("tipo", ctx.getTipo().name());
        resp.put("status", ctx.getStatus().name());
        Object erro = ctx.getDados().get("__erro");
        if (erro != null) resp.put("erro", erro.toString());
        return ResponseEntity.ok(resp);
    }

    private ResponseEntity<SagaIniciadaResponseDTO> aceitar(UUID sagaId) {
        return ResponseEntity.accepted().body(new SagaIniciadaResponseDTO(sagaId, "INICIADA"));
    }
}
