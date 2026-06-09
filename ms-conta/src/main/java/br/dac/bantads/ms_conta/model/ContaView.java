package br.dac.bantads.ms_conta.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Modelo de leitura (CQRS read side).
 * Mantido sincronizado com ContaModel via ContaEventHandler.
 * Todas as queries do ContaController usam esta tabela.
 */
@Entity
@Table(name = "conta_view")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContaView {

    @Id
    @Column(name = "cv_uuid", nullable = false)
    private UUID uuidConta;

    @Column(name = "cv_cliente_uuid", nullable = false, unique = true)
    private UUID uuidCliente;

    @Column(name = "cv_numero", nullable = false, length = 4)
    private String numero;

    @Column(name = "cv_data_criacao", nullable = false)
    private LocalDate dataCriacao;

    @Column(name = "cv_saldo", nullable = false, precision = 19, scale = 2)
    private BigDecimal saldo;

    @Column(name = "cv_limite", nullable = false, precision = 19, scale = 2)
    private BigDecimal limite;

    @Column(name = "cv_gerente_uuid", nullable = false)
    private UUID uuidGerente;

    @Column(name = "cv_ativo", nullable = false)
    private boolean ativo;

    @Column(name = "cv_rejeitado_motivo", length = 200)
    private String rejeitadoMotivo;

    @Column(name = "cv_rejeitado_data")
    private LocalDate rejeitadoData;
}
