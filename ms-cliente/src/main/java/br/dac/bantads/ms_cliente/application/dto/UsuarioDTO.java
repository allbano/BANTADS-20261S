package br.dac.bantads.ms_cliente.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("_id")
    private String id;
    
    private String email;
    private String senha;
    private String cargo;
    private boolean ativo;
}
