package br.dac.bantads.ms_saga.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Filas existentes — saga envia comandos, serviços já escutam
    public static final String FILA_REGISTRO_CLIENTE        = "FILA_REGISTRO_CLIENTE";
    public static final String FILA_REGISTRO_CONTA_CLIENTE  = "FILA_REGISTRO_CONTA_CLIENTE";
    public static final String FILA_AUTENTICACAO            = "FILA_AUTENTICACAO";
    public static final String FILA_ERRO_NOVO_CLIENTE       = "FILA_ERRO_NOVO_CLIENTE";

    // Fila de compensação — saga envia, ms-cliente escuta para excluir cliente
    public static final String SAGA_CMD_EXCLUIR_CLIENTE = "SAGA_CMD_EXCLUIR_CLIENTE";

    // Eventos de resposta — serviços publicam, saga escuta
    public static final String SAGA_EVT_CLIENTE_CRIADO = "SAGA_EVT_CLIENTE_CRIADO";
    public static final String SAGA_EVT_CONTA_CRIADA   = "SAGA_EVT_CONTA_CRIADA";
    public static final String SAGA_EVT_AUTH_CRIADO    = "SAGA_EVT_AUTH_CRIADO";
    public static final String SAGA_EVT_CLIENTE_ERRO   = "SAGA_EVT_CLIENTE_ERRO";
    public static final String SAGA_EVT_CONTA_ERRO     = "SAGA_EVT_CONTA_ERRO";
    public static final String SAGA_EVT_AUTH_ERRO      = "SAGA_EVT_AUTH_ERRO";

    @Bean Queue registroClienteQueue()       { return new Queue(FILA_REGISTRO_CLIENTE, true); }
    @Bean Queue registroContaClienteQueue()  { return new Queue(FILA_REGISTRO_CONTA_CLIENTE, true); }
    @Bean Queue autenticacaoQueue()          { return new Queue(FILA_AUTENTICACAO, true); }
    @Bean Queue erroNovoClienteQueue()       { return new Queue(FILA_ERRO_NOVO_CLIENTE, true); }
    @Bean Queue sagaCmdExcluirClienteQueue() { return new Queue(SAGA_CMD_EXCLUIR_CLIENTE, true); }
    @Bean Queue sagaEvtClienteCriadoQueue()  { return new Queue(SAGA_EVT_CLIENTE_CRIADO, true); }
    @Bean Queue sagaEvtContaCriadaQueue()    { return new Queue(SAGA_EVT_CONTA_CRIADA, true); }
    @Bean Queue sagaEvtAuthCriadoQueue()     { return new Queue(SAGA_EVT_AUTH_CRIADO, true); }
    @Bean Queue sagaEvtClienteErroQueue()    { return new Queue(SAGA_EVT_CLIENTE_ERRO, true); }
    @Bean Queue sagaEvtContaErroQueue()      { return new Queue(SAGA_EVT_CONTA_ERRO, true); }
    @Bean Queue sagaEvtAuthErroQueue()       { return new Queue(SAGA_EVT_AUTH_ERRO, true); }

    // ObjectMapper com suporte a Java time — usado para serialização manual nas filas
    @Bean
    ObjectMapper objectMapper() {
        return JsonMapper.builder().findAndAddModules().build();
    }
}
