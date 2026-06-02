package br.dac.bantads.ms_conta.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificacaoRabbitDTO {
    private boolean status;
    private String message;
    private String uuidCliente;
    private String idUsuario; // backward compatibility
}
