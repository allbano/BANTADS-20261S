package br.dac.bantads.ms_conta.dto.contrato;

import java.math.BigDecimal;

/** Contrato test_dac: POST /contas/{numero}/depositar e /sacar. */
public record OperacaoResponse(String conta, String data, BigDecimal saldo) {}
