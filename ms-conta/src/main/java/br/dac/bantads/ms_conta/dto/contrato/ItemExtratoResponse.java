package br.dac.bantads.ms_conta.dto.contrato;

import java.math.BigDecimal;

/** Item do extrato (test_dac). tipo ∈ {saque, depósito, transferência}. */
public record ItemExtratoResponse(
        String data, String tipo, String origem, String destino, BigDecimal valor) {}
