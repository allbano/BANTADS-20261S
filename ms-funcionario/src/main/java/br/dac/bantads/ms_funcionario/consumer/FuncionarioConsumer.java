package br.dac.bantads.ms_funcionario.consumer;

import br.dac.bantads.ms_funcionario.dto.FuncionarioRequestDTO;
import br.dac.bantads.ms_funcionario.service.FuncionarioService;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class FuncionarioConsumer {

    @Autowired
    private FuncionarioService service;

    @RabbitListener(queues = "funcionario-queue")
    public void listenFuncionarioQueue(@Payload FuncionarioRequestDTO funcionarioDTO) {
        try {
            System.out.println("Mensagem recebida para o funcionário: " + funcionarioDTO.getNome());
            service.saveOrUpdate(funcionarioDTO);
        } catch (Exception e) {
            System.err.println("Erro ao processar fila de funcionários: " + e.getMessage());
        }
    }
}