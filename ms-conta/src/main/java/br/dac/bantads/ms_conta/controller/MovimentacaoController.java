package br.dac.bantads.ms_conta.controller;

import br.dac.bantads.ms_conta.dto.ExtratoRequestDTO;
import br.dac.bantads.ms_conta.dto.MovimentacaoRequestDTO;
import br.dac.bantads.ms_conta.dto.MovimentacaoResponseDTO;
import br.dac.bantads.ms_conta.model.cud.MovimentacaoModel;
import br.dac.bantads.ms_conta.service.MovimentacaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping("/contas")
@RequiredArgsConstructor
@Slf4j
public class MovimentacaoController {

    private final MovimentacaoService movimentacaoService;

    @PostMapping("/{uuidConta}/movimentacoes")
    public ResponseEntity<MovimentacaoResponseDTO> realizarMovimentacao(
            @PathVariable UUID uuidConta,
            @RequestBody MovimentacaoRequestDTO request) {
        log.info("Recebida requisição para realizar movimentação na conta: {}", uuidConta);
        MovimentacaoModel model = movimentacaoService.realizarMovimentacao(uuidConta, request);
        return ResponseEntity.ok(toDTO(model));
    }

    @GetMapping("/{uuidConta}/movimentacoes")
    public ResponseEntity<List<MovimentacaoResponseDTO>> obterMovimentacoes(@PathVariable UUID uuidConta) {
        log.info("Recebida requisição para listar todas as movimentações da conta: {}", uuidConta);
        List<MovimentacaoResponseDTO> dtos = movimentacaoService.obterMovimentacoes(uuidConta).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/{uuidConta}/extrato")
    public ResponseEntity<List<MovimentacaoResponseDTO>> obterExtratoPost(
            @PathVariable UUID uuidConta,
            @RequestBody ExtratoRequestDTO request) {
        log.info("Recebida requisição POST para extrato da conta: {}", uuidConta);
        LocalDateTime dataInicio = request.dataInicio() != null ? 
                request.dataInicio().atStartOfDay() : LocalDate.now().minusDays(30).atStartOfDay();
        LocalDateTime dataFim = request.dataFim() != null ? 
                request.dataFim().atTime(LocalTime.MAX) : LocalDate.now().atTime(LocalTime.MAX);
        
        List<MovimentacaoResponseDTO> dtos = movimentacaoService.obterExtrato(uuidConta, dataInicio, dataFim).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{uuidConta}/extrato")
    public ResponseEntity<List<MovimentacaoResponseDTO>> obterExtratoGet(
            @PathVariable UUID uuidConta,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        log.info("Recebida requisição GET para extrato da conta: {}", uuidConta);
        LocalDateTime inicio = dataInicio != null ? 
                dataInicio.atStartOfDay() : LocalDate.now().minusDays(30).atStartOfDay();
        LocalDateTime fim = dataFim != null ? 
                dataFim.atTime(LocalTime.MAX) : LocalDate.now().atTime(LocalTime.MAX);
        
        List<MovimentacaoResponseDTO> dtos = movimentacaoService.obterExtrato(uuidConta, inicio, fim).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    private MovimentacaoResponseDTO toDTO(MovimentacaoModel model) {
        return new MovimentacaoResponseDTO(
                model.getUuidMovimentacao(),
                model.getDataHora(),
                model.getTipo(),
                model.getConta() != null ? model.getConta().getUuidConta() : null,
                model.getUuidContaDestino(),
                model.getValor()
        );
    }
}
