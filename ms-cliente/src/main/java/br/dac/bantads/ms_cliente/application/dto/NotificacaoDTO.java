package br.dac.bantads.ms_cliente.application.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacaoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean status;
    private String message;
    private Long idUsuario;
}
