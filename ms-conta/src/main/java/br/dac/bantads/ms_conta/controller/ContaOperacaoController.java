package br.dac.bantads.ms_conta.controller;

import br.dac.bantads.ms_conta.dto.MovimentacaoRequestDTO;
import br.dac.bantads.ms_conta.dto.contrato.ExtratoResponse;
import br.dac.bantads.ms_conta.dto.contrato.ItemExtratoResponse;
import br.dac.bantads.ms_conta.dto.contrato.OperacaoResponse;
import br.dac.bantads.ms_conta.dto.contrato.SaldoResponse;
import br.dac.bantads.ms_conta.dto.contrato.TransferenciaResponse;
import br.dac.bantads.ms_conta.model.cud.ContaModel;
import br.dac.bantads.ms_conta.model.cud.MovimentacaoModel;
import br.dac.bantads.ms_conta.model.enums.TipoMovimentacao;
import br.dac.bantads.ms_conta.repository.cud.ContaRepository;
import br.dac.bantads.ms_conta.repository.cud.MovimentacaoRepository;
import br.dac.bantads.ms_conta.service.MovimentacaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Operações de conta POR NÚMERO no formato do contrato test_dac
 * (saldo, depositar, sacar, transferir, extrato). Reusa o
 * {@link MovimentacaoService} (validações de saldo+limite e transferência já
 * existentes) e traduz UUID→número/CPF na borda. Datas em ISO-8601 com offset.
 */
@CrossOrigin
@RestController
@RequestMapping("/contas")
@RequiredArgsConstructor
@Slf4j
public class ContaOperacaoController {

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    private final ContaRepository contaRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final MovimentacaoService movimentacaoService;

    public record ValorRequest(BigDecimal valor) {}
    public record TransferenciaRequest(String destino, BigDecimal valor) {}

    @GetMapping("/{numero}/saldo")
    public SaldoResponse saldo(@PathVariable String numero) {
        ContaModel c = conta(numero);
        return new SaldoResponse(c.getClienteCpf(), c.getNumero(), c.getSaldo());
    }

    @PostMapping("/{numero}/depositar")
    public OperacaoResponse depositar(@PathVariable String numero, @RequestBody ValorRequest req) {
        ContaModel c = conta(numero);
        MovimentacaoModel m = movimentacaoService.realizarMovimentacao(c.getUuidConta(),
                new MovimentacaoRequestDTO(TipoMovimentacao.DEPOSITO, req.valor(), null));
        return new OperacaoResponse(numero, fmt(m.getDataHora()), m.getConta().getSaldo());
    }

    @PostMapping("/{numero}/sacar")
    public OperacaoResponse sacar(@PathVariable String numero, @RequestBody ValorRequest req) {
        ContaModel c = conta(numero);
        MovimentacaoModel m = movimentacaoService.realizarMovimentacao(c.getUuidConta(),
                new MovimentacaoRequestDTO(TipoMovimentacao.SAQUE, req.valor(), null));
        return new OperacaoResponse(numero, fmt(m.getDataHora()), m.getConta().getSaldo());
    }

    @PostMapping("/{numero}/transferir")
    public TransferenciaResponse transferir(@PathVariable String numero, @RequestBody TransferenciaRequest req) {
        ContaModel origem = conta(numero);
        ContaModel destino = conta(req.destino());
        MovimentacaoModel m = movimentacaoService.realizarMovimentacao(origem.getUuidConta(),
                new MovimentacaoRequestDTO(TipoMovimentacao.TRANSFERENCIA, req.valor(), destino.getUuidConta()));
        return new TransferenciaResponse(numero, fmt(m.getDataHora()), destino.getNumero(),
                m.getConta().getSaldo(), req.valor());
    }

    @GetMapping("/{numero}/extrato")
    public ExtratoResponse extrato(@PathVariable String numero) {
        ContaModel c = conta(numero);
        List<ItemExtratoResponse> itens = movimentacaoRepository
                .findByConta_UuidContaOrderByDataHoraAsc(c.getUuidConta())
                .stream().map(m -> toItem(c, m)).toList();
        return new ExtratoResponse(numero, c.getSaldo(), itens);
    }

    // ── helpers ──
    private ContaModel conta(String numero) {
        return contaRepository.findByNumero(numero)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta não encontrada: " + numero));
    }

    private String fmt(LocalDateTime dt) {
        return dt == null ? null
                : dt.atZone(ZONE).truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private String numeroDe(UUID uuidConta) {
        return uuidConta == null ? null
                : contaRepository.findById(uuidConta).map(ContaModel::getNumero).orElse(null);
    }

    private ItemExtratoResponse toItem(ContaModel propria, MovimentacaoModel m) {
        String tipo = switch (m.getTipo()) {
            case DEPOSITO -> "depósito";
            case SAQUE -> "saque";
            case TRANSFERENCIA -> "transferência";
        };
        String origem = null, destino = null;
        switch (m.getTipo()) {
            // Contrato test_dac (R8): origem = a própria conta também no depósito.
            case DEPOSITO -> origem = propria.getNumero();
            case SAQUE -> origem = propria.getNumero();
            case TRANSFERENCIA -> {
                origem = propria.getNumero();
                destino = numeroDe(m.getUuidContaDestino());
            }
        }
        return new ItemExtratoResponse(fmt(m.getDataHora()), tipo, origem, destino, m.getValor());
    }
}
