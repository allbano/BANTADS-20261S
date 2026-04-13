package br.dac.bantads.ms_funcionario.model;

import java.io.Serial;
import java.util.UUID;

import com.fasterxml.uuid.Generators;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    @GeneratedValue(generator = "uuidv7")
    @Column(name = "funcionario_uuid", nullable = false, unique = true)
    private UUID uuidFuncionario;

    @Column(name = "funcionario_cpf", nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(name = "funcionario_nome", nullable = false)
    private String nome;

    @Column(name = "funcionario_email", nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "funcionario_tipo", nullable = false)
    private TipoFuncionario tipo;

    @PrePersist
    private void prePersist() {
        if (this.uuidFuncionario == null) {
            this.uuidFuncionario = Generators.timeBasedEpochGenerator().generate();
        }
    }
}
