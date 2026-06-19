package br.dac.bantads.ms_conta.controller;

import br.dac.bantads.ms_conta.dto.ContaResponseDTO;
import br.dac.bantads.ms_conta.service.ContaQueryService;
import br.dac.bantads.ms_conta.service.ContaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * MS Conta — consultas (lado de leitura do CQRS, {@link ContaQueryService}).
 * Serve a API Composition do gateway: contas por cliente/gerente, top 3 saldos
 * (R14) e a carteira para o dashboard do admin (R15).
 */
@CrossOrigin
@RestController
@RequestMapping("/contas")
@RequiredArgsConstructor
@Slf4j
public class ContaController {

    private final ContaQueryService contaQueryService;
    private final ContaService contaService;

    @GetMapping
    public ResponseEntity<List<ContaResponseDTO>> getAllContas() {
        log.info("Recebida requisição para listar todas as contas");
        return ResponseEntity.ok(contaQueryService.listarTodas());
    }

    @GetMapping("/top3")
    public ResponseEntity<List<ContaResponseDTO>> getTop3Contas() {
        log.info("Recebida requisição para listar as top 3 contas com maior saldo");
        return ResponseEntity.ok(contaQueryService.top3PorSaldo());
    }

    @GetMapping("/{uuidConta}")
    public ResponseEntity<ContaResponseDTO> getContaById(@PathVariable UUID uuidConta) {
        log.info("Recebida requisição para buscar conta por ID: {}", uuidConta);
        return ResponseEntity.ok(contaQueryService.buscarPorId(uuidConta));
    }

    @GetMapping("/cliente/{uuidCliente}")
    public ResponseEntity<ContaResponseDTO> getContaByCliente(@PathVariable UUID uuidCliente) {
        log.info("Recebida requisição para buscar conta pelo cliente UUID: {}", uuidCliente);
        return ResponseEntity.ok(contaQueryService.buscarPorCliente(uuidCliente));
    }

    @PostMapping("/cliente/{uuidCliente}/rejeitar")
    public ResponseEntity<Void> rejeitarPorCliente(@PathVariable UUID uuidCliente,
                                                   @RequestBody(required = false) Map<String, Object> body) {
        String motivo = body != null && body.get("motivo") != null ? String.valueOf(body.get("motivo")) : "";
        log.info("Recebida requisição para rejeitar a conta do cliente UUID: {}", uuidCliente);
        contaService.rejeitarConta(uuidCliente, motivo);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/por-gerente/{uuidGerente}")
    public ResponseEntity<List<ContaResponseDTO>> getContaPorGerenteId(@PathVariable UUID uuidGerente) {
        log.info("Recebida requisição para buscar contas pelo gerente UUID: {}", uuidGerente);
        return ResponseEntity.ok(contaQueryService.buscarPorGerente(uuidGerente));
    }
}
