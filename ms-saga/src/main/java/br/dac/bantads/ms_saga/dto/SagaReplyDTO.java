package br.dac.bantads.ms_saga.dto;

import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Resposta padrão de cada passo das SAGAs genéricas (Eixo 3). Publicada por
 * qualquer participante no canal {@code saga.reply}. {@code dados} carrega os
 * campos que o passo produziu (ex.: uuidCliente, numeroConta, senhaGerada) e é
 * mesclado ao contexto da saga para alimentar os passos seguintes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SagaReplyDTO {
    private UUID sagaId;
    private boolean sucesso;
    private String mensagem;
    private Map<String, Object> dados;
}
