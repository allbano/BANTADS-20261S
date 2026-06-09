package br.dac.bantads.ms_saga.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Publicado pelos serviços participantes (ms-cliente, ms-conta, ms-auth)
 * nas filas SAGA_EVT_* para informar o resultado de cada passo.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SagaRespostaDTO {
    private UUID sagaId;
    private boolean sucesso;
    private String mensagem;
    private String uuidCliente; // preenchido pelo ms-cliente no passo 1
}
