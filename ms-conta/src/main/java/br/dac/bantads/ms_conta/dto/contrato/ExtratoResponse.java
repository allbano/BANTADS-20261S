package br.dac.bantads.ms_conta.dto.contrato;

import java.math.BigDecimal;
import java.util.List;

/** Contrato test_dac: GET /contas/{numero}/extrato. */
public record ExtratoResponse(String conta, BigDecimal saldo, List<ItemExtratoResponse> movimentacoes) {}
