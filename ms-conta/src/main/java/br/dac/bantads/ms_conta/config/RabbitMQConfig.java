package br.dac.bantads.ms_conta.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topologia RabbitMQ do ms-conta.
 *
 * Exchange unico {@code bantads.topic} (Topic, durable); routing key = nome
 * logico da fila. Declara e faz binding apenas das filas que possui um
 * {@code @RabbitListener}; as demais constantes sao routing keys de publicacao.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "bantads.topic";
    public static final String DLX = "bantads.dlx";

    // Filas consumidas por este servico (declaradas + bound aqui)
    public static final String FILA_REGISTRO_CONTA_CLIENTE = "FILA_REGISTRO_CONTA_CLIENTE";
    public static final String FILA_ERRO_NOVO_CLIENTE = "FILA_ERRO_NOVO_CLIENTE";
    public static final String FILA_ATRIBUI_CONTA_GERENTE = "FILA_ATRIBUI_CONTA_GERENTE";

    // Routing keys usadas apenas para publicar
    public static final String SAGA_EVT_CONTA_CRIADA = "SAGA_EVT_CONTA_CRIADA";
    public static final String SAGA_EVT_CONTA_ERRO   = "SAGA_EVT_CONTA_ERRO";

    // CQRS interno do MS Conta: sync banco de Comando (conta_cud) -> Consulta (conta_r)
    public static final String RK_CONTA_ATUALIZADA = "conta.cqrs.atualizada";
    public static final String RK_CONTA_EXCLUIDA   = "conta.cqrs.excluida";
    public static final String QUEUE_CONTA_CQRS_ATUALIZADA = "conta.cqrs.atualizada.q";
    public static final String QUEUE_CONTA_CQRS_EXCLUIDA   = "conta.cqrs.excluida.q";

    // CQRS interno do MS Conta: replica movimentacao (conta_cud) -> movimentacao_view (conta_r)
    public static final String RK_MOVIMENTACAO_CRIADA = "movimentacao.cqrs.criada";
    public static final String QUEUE_MOVIMENTACAO_CQRS_CRIADA = "movimentacao.cqrs.criada.q";

    @Bean
    public TopicExchange bantadsTopic() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    public DirectExchange bantadsDlx() {
        return ExchangeBuilder.directExchange(DLX).durable(true).build();
    }

    @Bean Queue registroContaClienteQueue()  { return dlqEnabled(FILA_REGISTRO_CONTA_CLIENTE); }
    @Bean Queue erroNovoClienteQueue()        { return dlqEnabled(FILA_ERRO_NOVO_CLIENTE); }
    @Bean Queue atribuiContaGerenteQueue()    { return dlqEnabled(FILA_ATRIBUI_CONTA_GERENTE); }

    @Bean Queue registroContaClienteDlq()  { return dlq(FILA_REGISTRO_CONTA_CLIENTE); }
    @Bean Queue erroNovoClienteDlq()        { return dlq(FILA_ERRO_NOVO_CLIENTE); }
    @Bean Queue atribuiContaGerenteDlq()    { return dlq(FILA_ATRIBUI_CONTA_GERENTE); }

    @Bean Binding bRegistroContaCliente()  { return bind(registroContaClienteQueue(), FILA_REGISTRO_CONTA_CLIENTE); }
    @Bean Binding bErroNovoCliente()        { return bind(erroNovoClienteQueue(), FILA_ERRO_NOVO_CLIENTE); }
    @Bean Binding bAtribuiContaGerente()    { return bind(atribuiContaGerenteQueue(), FILA_ATRIBUI_CONTA_GERENTE); }

    @Bean Binding bDlqRegistroContaCliente()  { return bindDlq(registroContaClienteDlq(), FILA_REGISTRO_CONTA_CLIENTE); }
    @Bean Binding bDlqErroNovoCliente()        { return bindDlq(erroNovoClienteDlq(), FILA_ERRO_NOVO_CLIENTE); }
    @Bean Binding bDlqAtribuiContaGerente()    { return bindDlq(atribuiContaGerenteDlq(), FILA_ATRIBUI_CONTA_GERENTE); }

    // --- Eixo CQRS interno (sync conta_cud -> conta_r) ---
    @Bean Queue contaCqrsAtualizadaQueue() { return dlqEnabled(QUEUE_CONTA_CQRS_ATUALIZADA); }
    @Bean Queue contaCqrsExcluidaQueue()   { return dlqEnabled(QUEUE_CONTA_CQRS_EXCLUIDA); }
    @Bean Queue contaCqrsAtualizadaDlq()   { return dlq(QUEUE_CONTA_CQRS_ATUALIZADA); }
    @Bean Queue contaCqrsExcluidaDlq()     { return dlq(QUEUE_CONTA_CQRS_EXCLUIDA); }
    @Bean Binding bContaCqrsAtualizada() { return bind(contaCqrsAtualizadaQueue(), RK_CONTA_ATUALIZADA); }
    @Bean Binding bContaCqrsExcluida()   { return bind(contaCqrsExcluidaQueue(), RK_CONTA_EXCLUIDA); }
    @Bean Binding bDlqContaCqrsAtualizada() { return bindDlq(contaCqrsAtualizadaDlq(), QUEUE_CONTA_CQRS_ATUALIZADA); }
    @Bean Binding bDlqContaCqrsExcluida()   { return bindDlq(contaCqrsExcluidaDlq(), QUEUE_CONTA_CQRS_EXCLUIDA); }

    // --- Eixo CQRS interno (replica movimentacao -> movimentacao_view em conta_r) ---
    @Bean Queue movimentacaoCqrsCriadaQueue() { return dlqEnabled(QUEUE_MOVIMENTACAO_CQRS_CRIADA); }
    @Bean Queue movimentacaoCqrsCriadaDlq()   { return dlq(QUEUE_MOVIMENTACAO_CQRS_CRIADA); }
    @Bean Binding bMovimentacaoCqrsCriada()    { return bind(movimentacaoCqrsCriadaQueue(), RK_MOVIMENTACAO_CRIADA); }
    @Bean Binding bDlqMovimentacaoCqrsCriada() { return bindDlq(movimentacaoCqrsCriadaDlq(), QUEUE_MOVIMENTACAO_CQRS_CRIADA); }

    private static Queue dlqEnabled(String name) {
        return QueueBuilder.durable(name)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", name + ".dlq")
                .build();
    }

    private static Queue dlq(String name) {
        return QueueBuilder.durable(name + ".dlq").build();
    }

    private Binding bind(Queue queue, String routingKey) {
        return BindingBuilder.bind(queue).to(bantadsTopic()).with(routingKey);
    }

    private Binding bindDlq(Queue queue, String routingKey) {
        return BindingBuilder.bind(queue).to(bantadsDlx()).with(routingKey + ".dlq");
    }

    @Bean
    public ObjectMapper objectMapper() {
        // Registro explicito do JavaTimeModule: garante (de)serializacao de
        // LocalDate/LocalDateTime no fat-jar, sem depender do ServiceLoader.
        return JsonMapper.builder()
                .addModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
                .findAndAddModules()
                .build();
    }
}
