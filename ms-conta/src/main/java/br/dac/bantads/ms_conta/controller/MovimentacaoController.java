package br.dac.bantads.ms_conta.controller;

import br.dac.bantads.ms_conta.dto.MovimentacaoRequestDTO;
import br.dac.bantads.ms_conta.dto.MovimentacaoResponseDTO;
import br.dac.bantads.ms_conta.model.cud.MovimentacaoModel;
import br.dac.bantads.ms_conta.service.MovimentacaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * MS Conta — histórico de movimentações (depósito R5, saque R6, transferência R7)
 * usado internamente; as operações no formato do contrato test_dac (por número)
 * ficam no {@link ContaOperacaoController}.
 */
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

    // Extrato passou a ser servido por NÚMERO no contrato test_dac
    // (ver ContaOperacaoController GET /contas/{numero}/extrato).

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
