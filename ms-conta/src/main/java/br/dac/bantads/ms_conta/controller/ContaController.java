package br.dac.bantads.ms_conta.controller;

import br.dac.bantads.ms_conta.dto.ContaResponseDTO;
import br.dac.bantads.ms_conta.service.ContaQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/contas")
@RequiredArgsConstructor
@Slf4j
public class ContaController {

    private final ContaQueryService contaQueryService;

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

    @GetMapping("/por-usuario/{userId}")
    public ResponseEntity<ContaResponseDTO> getContaPorUserId(@PathVariable String userId) {
        log.info("Recebida requisição legado para buscar conta por ID de usuário: {}", userId);
        try {
            UUID clientUuid = UUID.fromString(userId);
            return getContaByCliente(clientUuid);
        } catch (IllegalArgumentException e) {
            log.warn("O ID de usuário fornecido não é um UUID válido: {}", userId);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/por-gerente/{uuidGerente}")
    public ResponseEntity<List<ContaResponseDTO>> getContaPorGerenteId(@PathVariable UUID uuidGerente) {
        log.info("Recebida requisição para buscar contas pelo gerente UUID: {}", uuidGerente);
        return ResponseEntity.ok(contaQueryService.buscarPorGerente(uuidGerente));
    }

    @GetMapping("/melhores/{uuidGerente}")
    public ResponseEntity<List<ContaResponseDTO>> getMelhoresPorGerenteId(@PathVariable UUID uuidGerente) {
        log.info("Recebida requisição para buscar as melhores contas do gerente UUID: {}", uuidGerente);
        return ResponseEntity.ok(contaQueryService.melhoresPorGerente(uuidGerente));
    }

    @GetMapping("/pendentes/{uuidGerente}")
    public ResponseEntity<List<ContaResponseDTO>> getPendentesPorGerenteId(@PathVariable UUID uuidGerente) {
        log.info("Recebida requisição para buscar contas pendentes do gerente UUID: {}", uuidGerente);
        return ResponseEntity.ok(contaQueryService.pendentesPorGerente(uuidGerente));
    }
}
