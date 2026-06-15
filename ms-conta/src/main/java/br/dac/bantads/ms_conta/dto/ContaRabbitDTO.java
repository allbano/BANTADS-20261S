package br.dac.bantads.ms_conta.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContaRabbitDTO {
    private String id;
    private String idUsuario;
    private String uuidCliente;
    private String cpf;
    private String idGerente;
    private String uuidGerente;

    // Preenchido pelo ms-saga quando orquestra o fluxo; null para fluxo legado
    private String sagaId;
    private BigDecimal saldo;
    private BigDecimal salario;
    private boolean ativo;
    private String rejeitadoMotivo;

    public UUID parseClientUuid() {
        String clientStr = uuidCliente != null && !uuidCliente.isBlank() ? uuidCliente : idUsuario;
        if (clientStr == null || clientStr.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(clientStr);
        } catch (IllegalArgumentException e) {
            // It could be a Long ID, or format mismatch. Return null or generate/handle differently.
            return null;
        }
    }

    public UUID parseGerenteUuid() {
        String gerenteStr = uuidGerente != null && !uuidGerente.isBlank() ? uuidGerente : idGerente;
        if (gerenteStr == null || gerenteStr.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(gerenteStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public UUID parseContaUuid() {
        if (id == null || id.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
