package br.dac.bantads.ms_cliente.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnderecoModel {
    @Column(name = "cliente_logradouro")
    private String logradouro;

    @Column(name = "cliente_numero")
    private String numero;

    @Column(name = "cliente_complemento")
    private String complemento;

    @Column(name = "cliente_cep")
    private String cep;

    @Column(name = "cliente_cidade")
    private String cidade;

    @Column(name = "cliente_estado")
    private String estado;
}