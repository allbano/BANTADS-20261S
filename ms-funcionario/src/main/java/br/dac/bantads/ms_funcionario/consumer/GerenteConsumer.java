package br.dac.bantads.ms_funcionario.consumer;

import br.dac.bantads.ms_funcionario.configuration.RabbitMq;
import br.dac.bantads.ms_funcionario.domain.FuncionarioModel;
import br.dac.bantads.ms_funcionario.dto.enums.TipoFuncionario;
import br.dac.bantads.ms_funcionario.dto.GerenteDTO;
import br.dac.bantads.ms_funcionario.dto.Usuario;
import br.dac.bantads.ms_funcionario.service.FuncionarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class GerenteConsumer {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FuncionarioService service;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMq.FILA_CREATE_GERENTE)
    public void registraNovoGerente(String msg) {
        try {
            System.out.println("Mensagem recebida na fila FILA_CREATE_GERENTE: " + msg);
            GerenteDTO gerenteDto = objectMapper.readValue(msg, GerenteDTO.class);

            // Verifica unicidade de CPF e Email
            if (service.getByCpf(gerenteDto.getCpf()).isPresent()) {
                System.err.println("Já existe um funcionário com este CPF: " + gerenteDto.getCpf());
                return;
            }

            if (service.getByEmail(gerenteDto.getEmail()).isPresent()) {
                System.err.println("Já existe um funcionário com este Email: " + gerenteDto.getEmail());
                return;
            }

            // Mapeia o ID do DTO se for um UUID válido, senão gera um novo
            UUID uuid = null;
            if (gerenteDto.getId() != null && !gerenteDto.getId().isBlank()) {
                try {
                    uuid = UUID.fromString(gerenteDto.getId());
                } catch (IllegalArgumentException e) {
                    System.out.println("ID recebido não é um UUID válido, um novo UUIDv7 será gerado.");
                }
            }

            FuncionarioModel g = FuncionarioModel.builder()
                    .uuid(uuid)
                    .cpf(gerenteDto.getCpf())
                    .nome(gerenteDto.getNome())
                    .email(gerenteDto.getEmail())
                    .telefone(gerenteDto.getTelefone())
                    .senha(gerenteDto.getSenha())
                    .tipo(TipoFuncionario.GERENTE)
                    .build();

            FuncionarioModel novoGerente = service.saveFuncionario(g);
            System.out.println("Gerente salvo: " + objectMapper.writeValueAsString(novoGerente));

            // Notifica atribuição de conta do gerente
            String jsonId = objectMapper.writeValueAsString(novoGerente.getUuid().toString());
            rabbitTemplate.convertAndSend(RabbitMq.EXCHANGE, RabbitMq.FILA_ATRIBUI_CONTA_GERENTE, jsonId);

            // Notifica autenticação
            Usuario uAuth = new Usuario(
                    novoGerente.getUuid().toString(),
                    novoGerente.getEmail(),
                    novoGerente.getSenha(),
                    "GERENTE",
                    true
            );
            String jsonAuth = objectMapper.writeValueAsString(uAuth);
            rabbitTemplate.convertAndSend(RabbitMq.EXCHANGE, RabbitMq.FILA_AUTENTICACAO, jsonAuth);

        } catch (Exception e) {
            System.err.println("Erro ao processar fila FILA_CREATE_GERENTE: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @RabbitListener(queues = RabbitMq.FILA_DELETE_GERENTE)
    public void deletaGerente(String msg) {
        try {
            System.out.println("Mensagem recebida na fila FILA_DELETE_GERENTE: " + msg);
            
            String gerenteIdStr = null;
            try {
                gerenteIdStr = objectMapper.readValue(msg, String.class);
            } catch (Exception e) {
                try {
                    Long idLong = objectMapper.readValue(msg, Long.class);
                    gerenteIdStr = String.valueOf(idLong);
                } catch (Exception ex) {
                    gerenteIdStr = msg.replace("\"", "").trim();
                }
            }

            Optional<FuncionarioModel> opt = Optional.empty();
            try {
                UUID uuid = UUID.fromString(gerenteIdStr);
                opt = service.getByUuid(uuid);
            } catch (IllegalArgumentException e) {
                System.out.println("O ID fornecido não é um UUID válido: " + gerenteIdStr);
            }

            if (opt.isPresent()) {
                FuncionarioModel gerente = opt.get();
                service.delete(gerente.getUuid());
                System.out.println("Gerente excluído com sucesso: " + gerente.getUuid());

                // Notifica autenticação para desativar
                Usuario uAuth = new Usuario(
                        gerente.getUuid().toString(),
                        null,
                        null,
                        null,
                        false
                );
                String jsonAuth = objectMapper.writeValueAsString(uAuth);
                rabbitTemplate.convertAndSend(RabbitMq.EXCHANGE, RabbitMq.FILA_AUTENTICACAO, jsonAuth);
            } else {
                System.err.println("Nenhum gerente encontrado com o ID fornecido: " + gerenteIdStr);
            }

        } catch (Exception e) {
            System.err.println("Erro ao processar fila FILA_DELETE_GERENTE: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
