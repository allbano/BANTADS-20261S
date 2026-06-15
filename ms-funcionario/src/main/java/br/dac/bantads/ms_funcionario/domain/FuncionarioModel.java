package br.dac.bantads.ms_funcionario.domain;

import java.io.Serial;
import java.util.UUID;

import com.fasterxml.uuid.Generators;

import jakarta.persistence.*;
import lombok.*;
import br.dac.bantads.ms_funcionario.dto.enums.TipoFuncionario;

@Entity
@Table(name = "funcionario")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FuncionarioModel {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "funcionario_uuid",
            nullable = false,
            unique = true,
            updatable = false,
            columnDefinition = "uuid")
    private UUID uuid;

    @Column(name = "funcionario_cpf", nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(name = "funcionario_nome", nullable = false)
    private String nome;

    @Column(name = "funcionario_email", nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "funcionario_tipo", nullable = false)
    private TipoFuncionario tipo;

    @Column(name = "funcionario_telefone")
    private String telefone;

    @Column(name = "funcionario_senha")
    private String senha;

    @PrePersist
    private void prePersist() {
        if (this.uuid == null) {
            this.uuid = Generators.timeBasedEpochGenerator().generate();
        }
    }
}
