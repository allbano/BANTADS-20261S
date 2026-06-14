package br.dac.bantads.ms_conta.dto.contrato;

import java.math.BigDecimal;

/** Contrato test_dac: POST /contas/{numero}/transferir. */
public record TransferenciaResponse(
        String conta, String data, String destino, BigDecimal saldo, BigDecimal valor) {}
