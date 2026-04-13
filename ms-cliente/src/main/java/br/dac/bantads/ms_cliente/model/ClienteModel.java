package br.dac.bantads.ms_cliente.model;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;
import com.fasterxml.uuid.Generators;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cliente")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteModel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "uuidv7")
    @Column(name = "cliente_uuid", nullable = false, unique = true)
    private UUID uuidCliente;

    @Column(name = "cliente_nome", nullable = false)
    private String nome;

    @Column(name = "cliente_email", nullable = false, unique = true)
    private String email;

    @Column(name = "cliente_cpf", nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(name = "cliente_telefone")
    private String telefone;

    @Builder.Default
    @Column(name = "cliente_salario", precision = 19, scale = 2)
    private BigDecimal salario = BigDecimal.ZERO;

    @Embedded
    private EnderecoModel endereco;

    @PrePersist
    private void prePersist() {
        if (this.uuid == null) {
            this.uuid = Generators.timeBasedEpochGenerator().generate();
        }
    }
}