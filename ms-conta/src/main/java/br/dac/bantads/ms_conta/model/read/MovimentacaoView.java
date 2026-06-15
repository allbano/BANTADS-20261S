package br.dac.bantads.ms_conta.model.read;

import br.dac.bantads.ms_conta.model.enums.TipoMovimentacao;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modelo de leitura (CQRS read side) das movimentações.
 * Mantido sincronizado com MovimentacaoModel (conta_cud) via ContaCqrsConsumer.
 *
 * Projeção DENORMALIZADA: carrega o número da própria conta e o número da conta
 * de destino para montar o extrato (R8) inteiramente a partir do banco de leitura
 * (conta_r), sem joins nem lookup no banco de Comando.
 */
@Entity
@Table(name = "movimentacao_view")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimentacaoView {

    @Id
    @Column(name = "mv_uuid", nullable = false)
    private UUID uuidMovimentacao;

    @Column(name = "mv_conta_uuid", nullable = false)
    private UUID uuidConta;

    @Column(name = "mv_conta_numero", nullable = false, length = 4)
    private String numeroConta;

    @Column(name = "mv_data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(name = "mv_tipo", nullable = false)
    private TipoMovimentacao tipo;

    @Column(name = "mv_valor", nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @Column(name = "mv_conta_destino_uuid")
    private UUID uuidContaDestino;

    @Column(name = "mv_conta_destino_numero", length = 4)
    private String numeroContaDestino;
}
