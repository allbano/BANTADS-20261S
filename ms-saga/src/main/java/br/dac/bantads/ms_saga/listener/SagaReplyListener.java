package br.dac.bantads.ms_saga.listener;

import br.dac.bantads.ms_saga.dto.SagaReplyDTO;
import br.dac.bantads.ms_saga.orchestrator.GenericSagaOrchestrator;
import br.dac.bantads.ms_saga.saga.SagaRoutes;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Canal único de resposta das SAGAs genéricas: todos os participantes publicam
 * o resultado de cada passo em {@code saga.reply}; aqui delegamos ao orquestrador.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SagaReplyListener {

    private final GenericSagaOrchestrator orchestrator;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = SagaRoutes.REPLY)
    void onReply(String msg) {
        log.debug("Recebido saga.reply: {}", msg);
        try {
            SagaReplyDTO r = objectMapper.readValue(msg, SagaReplyDTO.class);
            if (r.getSagaId() == null) {
                log.warn("saga.reply sem sagaId ignorado: {}", msg);
                return;
            }
            orchestrator.onReply(r.getSagaId(), r.isSucesso(), r.getMensagem(), r.getDados());
        } catch (Exception e) {
            log.error("Erro ao processar saga.reply. Raw: {}", msg, e);
        }
    }
}
