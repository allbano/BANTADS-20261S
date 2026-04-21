package br.dac.bantads.ms_conta.model;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.uuid.Generators;

import br.dac.bantads.ms_conta.model.enums.TipoMovimentacao;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entidade que representa uma movimentação bancária no histórico da conta.
 *
 * Histórico de Movimentações: data/hora, tipo (depósito, saque, transferência),
 * cliente origem/destino (quando for transferência), valor da movimentação.
 */
@Entity
@Table(name = "movimentacao")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimentacaoModel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "uuidv7")
    @Column(name = "movimentacao_uuid", nullable = false, unique = true)
    private UUID uuidMovimentacao;

    /**
     * Data e hora em que a movimentação foi registrada.
     */
    @Column(name = "movimentacao_data_hora", nullable = false)
    private LocalDateTime dataHora;

    /**
     * Tipo da movimentação: DEPOSITO, SAQUE ou TRANSFERENCIA.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "movimentacao_tipo", nullable = false)
    private TipoMovimentacao tipo;

    /**
     * Conta à qual esta movimentação pertence (origem em caso de saque/transferência enviada,
     * ou destino em caso de depósito/transferência recebida).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movimentacao_conta_uuid", nullable = false)
    private ContaModel conta;

    /**
     * UUID da conta de destino (preenchido quando tipo = TRANSFERENCIA).
     * Referência à conta destino no caso de transferência enviada,
     * ou à conta origem no caso de transferência recebida.
     */
    @Column(name = "movimentacao_conta_destino_uuid")
    private UUID uuidContaDestino;

    /**
     * Valor da movimentação. Sempre positivo; o tipo determina se é entrada ou saída.
     */
    @Column(name = "movimentacao_valor", nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @PrePersist
    private void prePersist() {
        if (this.uuidMovimentacao == null) {
            this.uuidMovimentacao = Generators.timeBasedEpochGenerator().generate();
        }
        if (this.dataHora == null) {
            this.dataHora = LocalDateTime.now();
        }
    }
}
