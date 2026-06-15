package br.dac.bantads.ms_saga.controller;

import br.dac.bantads.ms_saga.dto.AutocadastroRequestDTO;
import br.dac.bantads.ms_saga.dto.SagaResultadoDTO;
import br.dac.bantads.ms_saga.orchestrator.AutocadastroSagaOrchestrator;
import br.dac.bantads.ms_saga.orchestrator.GenericSagaOrchestrator;
import br.dac.bantads.ms_saga.saga.GenericSagaContext;
import br.dac.bantads.ms_saga.saga.SagaInstance;
import br.dac.bantads.ms_saga.saga.SagaStatus;
import br.dac.bantads.ms_saga.saga.SagaTipo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Endpoints das SAGAs em modo SÍNCRONO (bloqueante): cada chamada espera a saga
 * concluir e devolve o resultado final, para o gateway montar a resposta no
 * contrato test_dac. {@code GET /saga/status/{id}} segue disponível para debug.
 */
@RestController
@RequestMapping("/saga")
@CrossOrigin
@RequiredArgsConstructor
public class SagaController {

    private final AutocadastroSagaOrchestrator orchestrator;
    private final GenericSagaOrchestrator generic;

    /** R1 — Autocadastro (bloqueante). Sucesso → 201; falha (ex.: CPF duplicado) → 409. */
    @PostMapping("/autocadastro")
    ResponseEntity<SagaResultadoDTO> iniciarAutocadastro(@RequestBody AutocadastroRequestDTO request) {
        SagaInstance saga = orchestrator.iniciarEAguardar(request);
        boolean ok = saga != null && saga.getStatus() == SagaStatus.CONCLUIDA;
        Map<String, Object> dados = new HashMap<>();
        if (saga != null && saga.getUuidCliente() != null) {
            dados.put("uuidCliente", saga.getUuidCliente().toString());
        }
        SagaResultadoDTO body = new SagaResultadoDTO(
                saga != null ? saga.getSagaId().toString() : null,
                "AUTOCADASTRO",
                saga != null ? saga.getStatus().name() : "DESCONHECIDA",
                ok,
                ok ? null : "Cliente já cadastrado/aguardando aprovação ou falha no autocadastro",
                dados);
        return ResponseEntity.status(ok ? HttpStatus.CREATED : HttpStatus.CONFLICT).body(body);
    }

    @GetMapping("/autocadastro/{sagaId}")
    ResponseEntity<Map<String, String>> consultarStatus(@PathVariable UUID sagaId) {
        SagaStatus status = orchestrator.consultarStatus(sagaId);
        if (status == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("sagaId", sagaId.toString(), "status", status.name()));
    }

    // ───────────────────────── SAGAs genéricas (Eixo 3) ─────────────────────────

    /** R10 — Aprovar cliente. */
    @PostMapping("/clientes/{cpf}/aprovar")
    ResponseEntity<SagaResultadoDTO> aprovarCliente(@PathVariable String cpf,
                                                    @RequestBody(required = false) Map<String, Object> body) {
        return responder(SagaTipo.APROVAR_CLIENTE, comCpf(body, cpf));
    }

    /** R4 — Alteração de perfil. */
    @PutMapping("/clientes/{cpf}")
    ResponseEntity<SagaResultadoDTO> alterarPerfil(@PathVariable String cpf,
                                                   @RequestBody Map<String, Object> body) {
        return responder(SagaTipo.ALTERAR_PERFIL, comCpf(body, cpf));
    }

    /** R17 — Inserção de gerente. */
    @PostMapping("/gerentes")
    ResponseEntity<SagaResultadoDTO> inserirGerente(@RequestBody Map<String, Object> body) {
        return responder(SagaTipo.INSERIR_GERENTE, new HashMap<>(body != null ? body : Map.of()));
    }

    /** R18 — Remoção de gerente. */
    @DeleteMapping("/gerentes/{cpf}")
    ResponseEntity<SagaResultadoDTO> removerGerente(@PathVariable String cpf,
                                                    @RequestBody(required = false) Map<String, Object> body) {
        return responder(SagaTipo.REMOVER_GERENTE, comCpf(body, cpf));
    }

    /** R20 — Alteração de gerente. */
    @PutMapping("/gerentes/{cpf}")
    ResponseEntity<SagaResultadoDTO> alterarGerente(@PathVariable String cpf,
                                                    @RequestBody Map<String, Object> body) {
        return responder(SagaTipo.ALTERAR_GERENTE, comCpf(body, cpf));
    }

    /** Status de uma SAGA genérica (debug). */
    @GetMapping("/status/{sagaId}")
    ResponseEntity<Map<String, String>> consultarGenerica(@PathVariable UUID sagaId) {
        GenericSagaContext ctx = generic.consultar(sagaId);
        if (ctx == null) return ResponseEntity.notFound().build();
        Map<String, String> resp = new HashMap<>();
        resp.put("sagaId", sagaId.toString());
        resp.put("tipo", ctx.getTipo().name());
        resp.put("status", ctx.getStatus().name());
        Object erro = ctx.getDados().get("__erro");
        if (erro != null) resp.put("erro", erro.toString());
        return ResponseEntity.ok(resp);
    }

    // ── helpers ──
    private Map<String, Object> comCpf(Map<String, Object> body, String cpf) {
        Map<String, Object> dados = new HashMap<>(body != null ? body : Map.of());
        dados.put("cpf", cpf);
        return dados;
    }

    private ResponseEntity<SagaResultadoDTO> responder(SagaTipo tipo, Map<String, Object> dados) {
        GenericSagaContext ctx = generic.iniciarEAguardar(tipo, dados);
        boolean ok = ctx != null && ctx.getStatus() == SagaStatus.CONCLUIDA;
        String erro = ctx != null && ctx.getDados().get("__erro") != null
                ? ctx.getDados().get("__erro").toString() : null;
        SagaResultadoDTO body = new SagaResultadoDTO(
                ctx != null ? ctx.getSagaId().toString() : null,
                tipo.name(),
                ctx != null ? ctx.getStatus().name() : "DESCONHECIDA",
                ok, erro,
                ctx != null ? ctx.getDados() : Map.of());
        return ResponseEntity.status(ok ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(body);
    }
}
