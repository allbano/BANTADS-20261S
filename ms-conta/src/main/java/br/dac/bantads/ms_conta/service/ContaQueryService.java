package br.dac.bantads.ms_conta.service;

import br.dac.bantads.ms_conta.dto.ContaResponseDTO;
import br.dac.bantads.ms_conta.model.read.ContaView;
import br.dac.bantads.ms_conta.repository.read.ContaViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * CQRS query side — todas as leituras passam por aqui usando ContaView.
 * Separado do ContaService (write side) para garantir que leitura e escrita
 * usem modelos de dados independentes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContaQueryService {

    private final ContaViewRepository contaViewRepository;

    public List<ContaResponseDTO> listarTodas() {
        return contaViewRepository.findAll().stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    public ContaResponseDTO buscarPorId(UUID uuidConta) {
        return contaViewRepository.findById(uuidConta)
                .map(this::toDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta não encontrada"));
    }

    public ContaResponseDTO buscarPorCliente(UUID uuidCliente) {
        return contaViewRepository.findByUuidCliente(uuidCliente)
                .map(this::toDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta não encontrada para o cliente"));
    }

    public List<ContaResponseDTO> top3PorSaldo() {
        return contaViewRepository.findTop3ByOrderBySaldoDesc().stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    public List<ContaResponseDTO> buscarPorGerente(UUID uuidGerente) {
        return contaViewRepository.findByUuidGerenteOrderByNumeroAsc(uuidGerente).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    public List<ContaResponseDTO> melhoresPorGerente(UUID uuidGerente) {
        return contaViewRepository.findByUuidGerenteOrderBySaldoDesc(uuidGerente).stream()
                .limit(5).map(this::toDTO).collect(Collectors.toList());
    }

    public List<ContaResponseDTO> pendentesPorGerente(UUID uuidGerente) {
        return contaViewRepository.findByUuidGerenteAndAtivo(uuidGerente, false).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    private ContaResponseDTO toDTO(ContaView v) {
        return new ContaResponseDTO(
                v.getUuidConta(), v.getUuidCliente(), v.getNumero(), v.getDataCriacao(),
                v.getSaldo(), v.getLimite(), v.getUuidGerente(), v.isAtivo(),
                v.getRejeitadoMotivo(), v.getRejeitadoData()
        );
    }
}
