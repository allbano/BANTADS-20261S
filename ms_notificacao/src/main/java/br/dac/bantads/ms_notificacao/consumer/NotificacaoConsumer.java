package br.dac.bantads.ms_notificacao.consumer;

import br.dac.bantads.ms_notificacao.config.RabbitMQConfig;
import br.dac.bantads.ms_notificacao.dto.NotificacaoDTO;
import br.dac.bantads.ms_notificacao.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumidor da fila {@code FILA_NOTIFICACAO}.
 *
 * E o coracao do ms_notificacao: fica "escutando" a fila e, a cada mensagem
 * recebida, desserializa o JSON em um {@link NotificacaoDTO} e pede ao
 * {@link EmailService} para disparar o e-mail correto.
 *
 * Publicadores tipicos: ms-auth (senha na aprovacao), ms-saga (falha de
 * autocadastro) e ms-cliente/ms-funcionario (rejeicao).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificacaoConsumer {

    private final EmailService emailService;
    private final ObjectMapper notificacaoObjectMapper;

    /**
     * Metodo invocado automaticamente pelo Spring AMQP sempre que chega uma
     * mensagem na fila. Recebe o payload como String (JSON), converte para o
     * DTO e delega o envio.
     *
     * Tratamento de erro: capturamos a excecao e apenas logamos. Assim a
     * mensagem e considerada consumida (ack) e NAO volta para a fila, evitando
     * um loop infinito de reentrega ("poison message"). Uma evolucao futura
     * seria encaminhar a mensagem com falha para uma Dead Letter Queue (DLX).
     */
    @RabbitListener(queues = RabbitMQConfig.FILA_NOTIFICACAO)
    public void receber(String mensagem) {
        log.info("Mensagem recebida em {}: {}", RabbitMQConfig.FILA_NOTIFICACAO, mensagem);
        try {
            NotificacaoDTO notificacao = notificacaoObjectMapper.readValue(mensagem, NotificacaoDTO.class);
            emailService.enviar(notificacao);
        } catch (Exception e) {
            log.error("Falha ao processar notificacao [{}]: {}", mensagem, e.getMessage(), e);
        }
    }
}
