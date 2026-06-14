package br.dac.bantads.ms_conta.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Payload de sincronizacao CQRS (lado Comando -> RabbitMQ -> lado Consulta).
 *
 * Carrega o snapshot denormalizado da conta para projecao na ContaView (banco
 * de leitura conta_r). Nao trafega entidade JPA — apenas este DTO.
 */
public record ContaSyncDTO(
        UUID uuidConta,
        UUID uuidCliente,
        String clienteCpf,
        String numero,
        LocalDate dataCriacao,
        BigDecimal saldo,
        BigDecimal limite,
        UUID uuidGerente,
        boolean ativo,
        String rejeitadoMotivo,
        LocalDate rejeitadoData
) {}
