package br.dac.bantads.ms_cliente.Model;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.uuid.Generators;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cliente")
@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ClienteModel {
	@Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "uuidv7")
    @Column(name = "cliente_uuid", nullable = false, unique = true)
    private UUID uuidCliente;
    
    @Column(name = "cliente_nome", nullable = false)
    private String nomeCliente;
    
    @Column(name = "cliente_email", nullable = false)
    private String emailCliente;

    @Column(name = "cliente_CPF", nullable = false)
    private String cpfCliente;

    @Column(name = "cliente_telefone", nullable = false)
    private String telefoneCliente;
    
    @Builder.Default
    @Column(name = "cliente_salario", precision = 19, scale = 2)
    private BigDecimal salario = BigDecimal.ZERO;
    
    @Column(name = "cliente_logradouro", nullable = false)
    private String logradouroCliente;
    
    @Column(name = "cliente_numero", nullable = false)
    private String numeroCliente;
    
    @Column(name = "cliente_complemento", nullable = false)
    private String complementoCliente;
    
    @Column(name = "cliente_CEP", nullable = false)
    private String cepCliente;
    
    @Column(name = "cliente_cidade", nullable = false)
    private String cidadeCliente;
    
    @Column(name = "cliente_estado", nullable = false)
    private String estadoCliente;
    
    @PrePersist
    private void prePersist() {
        if (this.uuidCliente == null) {
            this.uuidCliente = Generators.timeBasedEpochGenerator().generate();
        }
    }
}
