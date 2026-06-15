package br.dac.bantads.ms_cliente.infrastructure.messaging;

import br.dac.bantads.ms_cliente.application.dto.ClienteDTO;
import br.dac.bantads.ms_cliente.application.service.ClienteService;
import br.dac.bantads.ms_cliente.infrastructure.config.RabbitMQConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Consumidores RabbitMQ do ms-cliente nos fluxos de SAGA.
 *
 * R1 (Autocadastro): {@code registraNovoCliente} é o 1º passo da SAGA de
 * autocadastro orquestrada pelo ms-saga — cria o registro de Cliente e devolve
 * o evento de progresso/erro ao orquestrador.
 * Compensação: {@code excluirClienteSaga} desfaz a criação quando um passo
 * posterior da SAGA falha.
 */
@Component
public class RabbitMQConsumer {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ClienteService clienteService;

    // R1 — passo "MS Cliente: criação do registro de Cliente" da SAGA de autocadastro.
    @RabbitListener(queues = RabbitMQConfig.FILA_REGISTRO_CLIENTE)
    public void registraNovoCliente(String msg) {
        try {
            System.out.println("Mensagem recebida na fila REGISTRO_CLIENTE: " + msg);
            ClienteDTO cliente = objectMapper.readValue(msg, ClienteDTO.class);
            clienteService.processaNovoClienteEvent(cliente);
        } catch (JsonProcessingException e) {
            System.err.println("Erro de parsing JSON na fila REGISTRO_CLIENTE: " + e.getMessage());
        }
    }

    // Compensação saga: ms-saga manda excluir o cliente quando algum passo posterior falha
    @RabbitListener(queues = RabbitMQConfig.SAGA_CMD_EXCLUIR_CLIENTE)
    public void excluirClienteSaga(String msg) {
        System.out.println("Mensagem recebida na fila SAGA_CMD_EXCLUIR_CLIENTE: " + msg);
        try {
            String uuidStr = objectMapper.readValue(msg, String.class);
            clienteService.excluirClientePorUuid(uuidStr);
        } catch (Exception e) {
            System.err.println("Erro ao processar SAGA_CMD_EXCLUIR_CLIENTE: " + e.getMessage());
        }
    }
}
