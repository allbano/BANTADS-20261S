package br.dac.bantads.ms_funcionario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

    private String _id;
    private String email;
    private String senha;
    private String cargo;
    private boolean ativo;
}
