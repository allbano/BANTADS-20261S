package br.dac.bantads.ms_conta.dto.contrato;

import java.math.BigDecimal;

/** Contrato test_dac: GET /contas/{numero}/saldo. */
public record SaldoResponse(String cliente, String conta, BigDecimal saldo) {}
