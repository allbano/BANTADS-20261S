package br.dac.bantads.ms_conta.model;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.fasterxml.uuid.Generators;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidade que representa uma conta bancária no sistema BANTADS.
 *
 * Dados da Conta: Cliente, Número da conta, Data da criação, Saldo, Limite, Gerente
 */
@Entity
@Table(name = "conta")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContaModel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "uuidv7")
    @Column(name = "conta_uuid", nullable = false, unique = true)
    private UUID uuidConta;

    /**
     * UUID do cliente associado a esta conta (referência ao ms-cliente).
     */
    @Column(name = "conta_cliente_uuid", nullable = false, unique = true)
    private UUID uuidCliente;

    @Column(name = "conta_numero", nullable = false, unique = true, length = 4)
    private String numero;

    @Column(name = "conta_data_criacao", nullable = false)
    private LocalDate dataCriacao;

    /**
     * Saldo atual da conta. Pode ser negativo (até o limite).
     */
    @Builder.Default
    @Column(name = "conta_saldo", nullable = false, precision = 19, scale = 2)
    private BigDecimal saldo = BigDecimal.ZERO;

    /**
     * Limite da conta do cliente.
     * Calculado como metade do salário se salário >= R$ 2.000,00,
     * caso contrário é zero (R10).
     */
    @Builder.Default
    @Column(name = "conta_limite", nullable = false, precision = 19, scale = 2)
    private BigDecimal limite = BigDecimal.ZERO;

    /**
     * UUID do gerente responsável pela conta (referência ao ms-funcionario).
     */
    @Column(name = "conta_gerente_uuid", nullable = false)
    private UUID uuidGerente;

    /**
     * Lista de movimentações vinculadas a esta conta.
     */
    @OneToMany(mappedBy = "conta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MovimentacaoModel> movimentacoes;

    @PrePersist
    private void prePersist() {
        if (this.uuidConta == null) {
            this.uuidConta = Generators.timeBasedEpochGenerator().generate();
        }
    }
}
