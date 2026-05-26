package br.dac.bantads.ms_conta.controller;

import br.dac.bantads.ms_conta.dto.ContaResponseDTO;
import br.dac.bantads.ms_conta.model.ContaModel;
import br.dac.bantads.ms_conta.repository.ContaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/contas")
public class ContaController {

    private final ContaRepository contaRepository;

    public ContaController(ContaRepository contaRepository) {
        this.contaRepository = contaRepository;
    }

    @GetMapping
    public ResponseEntity<List<ContaResponseDTO>> getAllContas() {
        List<ContaResponseDTO> dtos = contaRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/top3")
    public ResponseEntity<List<ContaResponseDTO>> getTop3Contas() {
        List<ContaResponseDTO> dtos = contaRepository.findTop3ByOrderBySaldoDesc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/cliente/{uuidCliente}")
    public ResponseEntity<ContaResponseDTO> getContaByCliente(@PathVariable UUID uuidCliente) {
        return contaRepository.findByUuidCliente(uuidCliente)
                .map(this::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private ContaResponseDTO toDTO(ContaModel model) {
        return new ContaResponseDTO(
                model.getUuidConta(),
                model.getUuidCliente(),
                model.getNumero(),
                model.getDataCriacao(),
                model.getSaldo(),
                model.getLimite(),
                model.getUuidGerente()
        );
    }
}
