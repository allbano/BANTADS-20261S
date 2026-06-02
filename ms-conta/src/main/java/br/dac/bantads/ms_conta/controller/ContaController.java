package br.dac.bantads.ms_conta.controller;

import br.dac.bantads.ms_conta.dto.ContaResponseDTO;
import br.dac.bantads.ms_conta.model.ContaModel;
import br.dac.bantads.ms_conta.repository.ContaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping("/contas")
@RequiredArgsConstructor
@Slf4j
public class ContaController {

    private final ContaRepository contaRepository;

    @GetMapping
    public ResponseEntity<List<ContaResponseDTO>> getAllContas() {
        log.info("Recebida requisição para listar todas as contas");
        List<ContaResponseDTO> dtos = contaRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/top3")
    public ResponseEntity<List<ContaResponseDTO>> getTop3Contas() {
        log.info("Recebida requisição para listar as top 3 contas com maior saldo");
        List<ContaResponseDTO> dtos = contaRepository.findTop3ByOrderBySaldoDesc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{uuidConta}")
    public ResponseEntity<ContaResponseDTO> getContaById(@PathVariable UUID uuidConta) {
        log.info("Recebida requisição para buscar conta por ID: {}", uuidConta);
        return contaRepository.findById(uuidConta)
                .map(this::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cliente/{uuidCliente}")
    public ResponseEntity<ContaResponseDTO> getContaByCliente(@PathVariable UUID uuidCliente) {
        log.info("Recebida requisição para buscar conta pelo cliente UUID: {}", uuidCliente);
        return contaRepository.findByUuidCliente(uuidCliente)
                .map(this::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/por-usuario/{userId}")
    public ResponseEntity<ContaResponseDTO> getContaPorUserId(@PathVariable String userId) {
        log.info("Recebida requisição legado para buscar conta por ID de usuário: {}", userId);
        try {
            UUID clientUuid = UUID.fromString(userId);
            return getContaByCliente(clientUuid);
        } catch (IllegalArgumentException e) {
            log.warn("O ID de usuário fornecido não é um UUID válido: {}. Tentando tratar como id numérico...", userId);
            // Since our DB uses UUID, if a numeric Long ID is passed, we probably don't have a direct map
            // unless we log or look up. We return not found.
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/por-gerente/{uuidGerente}")
    public ResponseEntity<List<ContaResponseDTO>> getContaPorGerenteId(@PathVariable UUID uuidGerente) {
        log.info("Recebida requisição para buscar contas pelo gerente UUID: {}", uuidGerente);
        List<ContaResponseDTO> dtos = contaRepository.findByUuidGerenteOrderByNumeroAsc(uuidGerente).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/melhores/{uuidGerente}")
    public ResponseEntity<List<ContaResponseDTO>> getMelhoresPorGerenteId(@PathVariable UUID uuidGerente) {
        log.info("Recebida requisição para buscar as melhores contas do gerente UUID: {}", uuidGerente);
        List<ContaResponseDTO> dtos = contaRepository.findByUuidGerenteOrderBySaldoDesc(uuidGerente).stream()
                .limit(5)
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/pendentes/{uuidGerente}")
    public ResponseEntity<List<ContaResponseDTO>> getPendentesPorGerenteId(@PathVariable UUID uuidGerente) {
        log.info("Recebida requisição para buscar contas pendentes do gerente UUID: {}", uuidGerente);
        List<ContaResponseDTO> dtos = contaRepository.findByUuidGerenteAndAtivo(uuidGerente, false).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    private ContaResponseDTO toDTO(ContaModel model) {
        return new ContaResponseDTO(
                model.getUuidConta(),
                model.getUuidCliente(),
                model.getNumero(),
                model.getDataCriacao(),
                model.getSaldo(),
                model.getLimite(),
                model.getUuidGerente(),
                model.isAtivo(),
                model.getRejeitadoMotivo(),
                model.getRejeitadoData()
        );
    }
}
