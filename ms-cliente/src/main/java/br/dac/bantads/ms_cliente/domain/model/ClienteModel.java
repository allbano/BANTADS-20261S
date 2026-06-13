package br.dac.bantads.ms_cliente.domain.model;

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
    @Column(name = "cliente_uuid", nullable = false, unique = true)
    private UUID uuid;

    @Column(name = "cliente_nome", nullable = false)
    private String nome;

    @Column(name = "cliente_email", nullable = false, unique = true)
    private String email;

    @Column(name = "cliente_cpf", nullable = false, unique = true, length = 20)
    private String cpf;

    @Column(name = "cliente_telefone")
    private String telefone;

    @Builder.Default
    @Column(name = "cliente_salario", precision = 19, scale = 2)
    private BigDecimal salario = BigDecimal.ZERO;

    @Column(name = "cliente_endereco")
    private String endereco;

    @Column(name = "cliente_cep")
    private String cep;

    @Column(name = "cliente_cidade")
    private String cidade;

    @Column(name = "cliente_estado")
    private String estado;

    @Column(name = "cliente_senha")
    private String senha;

    @Builder.Default
    @Column(name = "cliente_ativo", nullable = false)
    private boolean ativo = true;

    @Builder.Default
    @Column(name = "cliente_cargo", nullable = false)
    private String cargo = "CLIENTE";

    @PrePersist
    private void prePersist() {
        if (this.uuid == null) {
            this.uuid = Generators.timeBasedEpochGenerator().generate();
        }
    }
}
