package br.dac.bantads.ms_saga.orchestrator;

import br.dac.bantads.ms_saga.saga.GenericSagaContext;
import br.dac.bantads.ms_saga.saga.SagaRoutes;
import br.dac.bantads.ms_saga.saga.SagaStatus;
import br.dac.bantads.ms_saga.saga.SagaStep;
import br.dac.bantads.ms_saga.saga.SagaTipo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Orquestrador genérico das 5 SAGAs do Eixo 3 (Aprovar R10, Perfil R4, Inserir
 * Gerente R17, Remover Gerente R18, Alterar Gerente R20).
 *
 * Padrão: para cada SAGA há uma definição ordenada de {@link SagaStep}. O
 * orquestrador publica o comando do passo corrente no Topic Exchange e aguarda
 * a resposta em {@code saga.reply}. Sucesso ⇒ mescla dados e avança; falha ⇒
 * compensa os passos já executados em ordem inversa. Estado em memória.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GenericSagaOrchestrator {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    private final Map<UUID, GenericSagaContext> store = new ConcurrentHashMap<>();

    private static final Map<SagaTipo, List<SagaStep>> DEFINICOES = Map.of(
            SagaTipo.APROVAR_CLIENTE, List.of(
                    SagaStep.of(SagaRoutes.CLIENTE_APROVAR, SagaRoutes.CLIENTE_REPROVAR),
                    SagaStep.of(SagaRoutes.CONTA_ATIVAR,    SagaRoutes.CONTA_DESATIVAR),
                    SagaStep.of(SagaRoutes.AUTH_GERAR_SENHA),
                    SagaStep.of(SagaRoutes.NOTIFICAR_CLIENTE)
            ),
            SagaTipo.ALTERAR_PERFIL, List.of(
                    SagaStep.of(SagaRoutes.CLIENTE_ATUALIZAR, SagaRoutes.CLIENTE_RESTAURAR),
                    SagaStep.of(SagaRoutes.CONTA_RECALC_LIMITE)
            ),
            SagaTipo.INSERIR_GERENTE, List.of(
                    SagaStep.of(SagaRoutes.GERENTE_INSERIR, SagaRoutes.GERENTE_EXCLUIR),
                    SagaStep.of(SagaRoutes.AUTH_CRIAR,      SagaRoutes.AUTH_REMOVER),
                    SagaStep.of(SagaRoutes.CONTA_ATRIBUIR_GERENTE)
            ),
            SagaTipo.REMOVER_GERENTE, List.of(
                    SagaStep.of(SagaRoutes.GERENTE_VALIDAR_REMOCAO),
                    SagaStep.of(SagaRoutes.CONTA_REDISTRIBUIR),
                    SagaStep.of(SagaRoutes.GERENTE_EXCLUIR),
                    SagaStep.of(SagaRoutes.AUTH_REMOVER)
            ),
            SagaTipo.ALTERAR_GERENTE, List.of(
                    SagaStep.of(SagaRoutes.GERENTE_ALTERAR),
                    SagaStep.of(SagaRoutes.AUTH_ATUALIZAR_SENHA)
            )
    );

    public UUID iniciar(SagaTipo tipo, Map<String, Object> dadosIniciais) {
        UUID sagaId = UUID.randomUUID();
        GenericSagaContext ctx = new GenericSagaContext(sagaId, tipo, DEFINICOES.get(tipo), dadosIniciais);
        store.put(sagaId, ctx);
        log.info("SAGA {} ({}) iniciada", sagaId, tipo);
        enviarPassoCorrente(ctx);
        return sagaId;
    }

    public void onReply(UUID sagaId, boolean sucesso, String mensagem, Map<String, Object> dados) {
        GenericSagaContext ctx = store.get(sagaId);
        if (ctx == null) { log.warn("SAGA {} não encontrada (onReply)", sagaId); return; }

        // Só avançamos enquanto a saga aguarda o passo corrente; respostas tardias
        // (ex.: confirmações de compensação) chegando em estado terminal são ignoradas.
        if (ctx.getStatus() != SagaStatus.EM_ANDAMENTO) {
            log.debug("SAGA {}: resposta ignorada (status {})", sagaId, ctx.getStatus());
            return;
        }

        if (!sucesso) {
            compensar(ctx, mensagem);
            return;
        }

        ctx.mesclarDados(dados);
        log.info("SAGA {} ({}): passo {} '{}' OK", sagaId, ctx.getTipo(),
                ctx.getPassoAtual(), ctx.passoCorrente().comando());
        ctx.avancar();

        if (ctx.concluido()) {
            ctx.setStatus(SagaStatus.CONCLUIDA);
            log.info("SAGA {} ({}): CONCLUÍDA", sagaId, ctx.getTipo());
        } else {
            enviarPassoCorrente(ctx);
        }
    }

    private void compensar(GenericSagaContext ctx, String motivo) {
        ctx.setStatus(SagaStatus.COMPENSANDO);
        ctx.getDados().put("__erro", motivo != null ? motivo : "falha em passo da saga");
        log.warn("SAGA {} ({}): compensando a partir do passo {}. Motivo: {}",
                ctx.getSagaId(), ctx.getTipo(), ctx.getPassoAtual(), motivo);

        // Compensa os passos já concluídos (anteriores ao corrente), em ordem inversa.
        for (int i = ctx.getPassoAtual() - 1; i >= 0; i--) {
            String comp = ctx.getPassos().get(i).compensacao();
            if (comp != null) {
                publicar(comp, ctx);
                log.info("SAGA {}: compensação '{}' publicada", ctx.getSagaId(), comp);
            }
        }
        ctx.setStatus(SagaStatus.FALHOU);
        log.error("SAGA {} ({}): FALHOU", ctx.getSagaId(), ctx.getTipo());
    }

    private void enviarPassoCorrente(GenericSagaContext ctx) {
        ctx.setStatus(SagaStatus.EM_ANDAMENTO);
        publicar(ctx.passoCorrente().comando(), ctx);
    }

    /** Publica um comando/compensação com o snapshot atual dos dados + sagaId. */
    private void publicar(String routingKey, GenericSagaContext ctx) {
        try {
            Map<String, Object> payload = new HashMap<>(ctx.getDados());
            payload.put("sagaId", ctx.getSagaId().toString());
            rabbitTemplate.convertAndSend(SagaRoutes.EXCHANGE, routingKey, objectMapper.writeValueAsString(payload));
            log.debug("SAGA {}: comando '{}' publicado", ctx.getSagaId(), routingKey);
        } catch (Exception e) {
            log.error("SAGA {}: falha ao publicar '{}'", ctx.getSagaId(), routingKey, e);
        }
    }

    public GenericSagaContext consultar(UUID sagaId) {
        return store.get(sagaId);
    }
}
