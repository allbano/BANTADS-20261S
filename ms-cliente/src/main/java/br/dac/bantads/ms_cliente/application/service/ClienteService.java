package br.dac.bantads.ms_cliente.application.service;

import br.dac.bantads.ms_cliente.application.dto.*;
import br.dac.bantads.ms_cliente.domain.model.ClienteModel;
import br.dac.bantads.ms_cliente.domain.repository.ClienteRepository;
import br.dac.bantads.ms_cliente.infrastructure.config.RabbitMQConfig;
import br.dac.bantads.ms_cliente.infrastructure.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final RestClient restClient;
    private final RabbitTemplate rabbitTemplate;
    private final MailService mailService;

    public record ContaResponseDTO(
            UUID uuidConta,
            UUID uuidCliente,
            String numero,
            LocalDate dataCriacao,
            BigDecimal saldo,
            BigDecimal limite,
            UUID uuidGerente
    ) {}

    public ClienteService(
            ClienteRepository clienteRepository,
            RabbitTemplate rabbitTemplate,
            MailService mailService,
            @Value("${services.conta:http://localhost:40006}") String contaServiceUrl) {
        this.clienteRepository = clienteRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.mailService = mailService;
        this.restClient = RestClient.builder().baseUrl(contaServiceUrl).build();
    }

    // --- Métodos Existentes mantidos intactos ---

    public List<ClienteParaAprovarResponse> getClientesParaAprovar() {
        List<ContaResponseDTO> contas = restClient.get()
                .uri("/contas")
                .retrieve()
                .body(new ParameterizedTypeReference<List<ContaResponseDTO>>() {});

        Set<UUID> approvedClientUuids = contas != null ?
                contas.stream().map(ContaResponseDTO::uuidCliente).collect(Collectors.toSet()) :
                Collections.emptySet();

        return clienteRepository.findAll().stream()
                .filter(c -> !approvedClientUuids.contains(c.getUuid()))
                .map(c -> new ClienteParaAprovarResponse(
                        c.getCpf(),
                        c.getNome(),
                        c.getEmail(),
                        c.getSalario(),
                        c.getEndereco(),
                        c.getCidade(),
                        c.getEstado()
                ))
                .collect(Collectors.toList());
    }

    public List<ClienteResponse> getMelhoresClientes() {
        List<ContaResponseDTO> top3Contas = restClient.get()
                .uri("/contas/top3")
                .retrieve()
                .body(new ParameterizedTypeReference<List<ContaResponseDTO>>() {});

        if (top3Contas == null) {
            return Collections.emptyList();
        }

        return top3Contas.stream()
                .map(conta -> {
                    Optional<ClienteModel> clientOpt = clienteRepository.findById(conta.uuidCliente());
                    return clientOpt.map(c -> new ClienteResponse(
                            c.getCpf(),
                            c.getNome(),
                            c.getEmail(),
                            c.getTelefone(),
                            c.getEndereco(),
                            c.getCidade(),
                            c.getEstado(),
                            conta.numero(),
                            conta.saldo(),
                            conta.limite()
                    )).orElse(null);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<ClienteResponse> getClientesComConta() {
        List<ContaResponseDTO> contas = restClient.get()
                .uri("/contas")
                .retrieve()
                .body(new ParameterizedTypeReference<List<ContaResponseDTO>>() {});

        if (contas == null) {
            return Collections.emptyList();
        }

        return contas.stream()
                .map(conta -> {
                    Optional<ClienteModel> clientOpt = clienteRepository.findById(conta.uuidCliente());
                    return clientOpt.map(c -> new ClienteResponse(
                            c.getCpf(),
                            c.getNome(),
                            c.getEmail(),
                            c.getTelefone(),
                            c.getEndereco(),
                            c.getCidade(),
                            c.getEstado(),
                            conta.numero(),
                            conta.saldo(),
                            conta.limite()
                    )).orElse(null);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // --- Novos métodos modernizados para suporte REST ---

    public List<ClienteResponseDTO> listAll() {
        return clienteRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public Optional<ClienteResponseDTO> getById(UUID uuid) {
        return clienteRepository.findById(uuid).map(this::toResponseDTO);
    }

    public Optional<ClienteResponseDTO> getByCpf(String cpf) {
        return clienteRepository.findByCpf(cpf).map(this::toResponseDTO);
    }

    public Optional<ClienteResponseDTO> getByEmail(String email) {
        // Como o repositório de domínio do destino não tem findByEmail, vamos filtrar da lista ou adicionar ao repository.
        // Vamos buscar da lista de findAll() para manter a interface de repositório de domínio limpa, ou podemos usar findAll().
        return clienteRepository.findAll().stream()
                .filter(c -> c.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .map(this::toResponseDTO);
    }

    public ClienteResponseDTO cadastro(ClienteRequestDTO request) {
        if (clienteRepository.findByCpf(request.cpf()).isPresent()) {
            mailService.sendMail(request.email(), "BANTADS - Falha na criação da conta!",
                    "Uma falha ocorreu ao criar sua conta: CPF já está sendo utilizado!");
            throw new IllegalStateException("CPF já cadastrado!");
        }

        String plainPassword = SecurityUtils.generateStrongPassword();

        ClienteModel model = ClienteModel.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(plainPassword) // Salva em texto puro inicialmente conforme a lógica legada
                .cpf(request.cpf())
                .telefone(request.telefone())
                .salario(request.salario() != null ? request.salario() : BigDecimal.ZERO)
                .endereco(request.endereco())
                .cep(request.cep())
                .cidade(request.cidade())
                .estado(request.estado())
                .ativo(false) // Desativado até aprovação
                .cargo("CLIENTE")
                .build();

        ClienteModel saved = clienteRepository.save(model);

        mailService.sendMail(saved.getEmail(), "BANTADS - Conta criada!",
                "Sua conta foi criada com a senha: " + saved.getSenha() + "\nAgora é só esperar a aprovação por um de nossos gerentes!");

        return toResponseDTO(saved);
    }

    public ClienteResponseDTO update(UUID uuid, ClienteRequestDTO request) {
        ClienteModel existing = clienteRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado!"));

        boolean wasInactive = !existing.isAtivo();

        existing.setNome(request.nome());
        existing.setEmail(request.email());
        if (request.senha() != null && !request.senha().isBlank()) {
            existing.setSenha(SecurityUtils.hash(request.senha()));
        }
        existing.setCpf(request.cpf());
        existing.setTelefone(request.telefone());
        if (request.salario() != null) {
            existing.setSalario(request.salario());
        }
        existing.setEndereco(request.endereco());
        existing.setCep(request.cep());
        existing.setCidade(request.cidade());
        existing.setEstado(request.estado());
        if (request.ativo() != null) {
            existing.setAtivo(request.ativo());
        }
        if (request.cargo() != null) {
            existing.setCargo(request.cargo());
        }

        ClienteModel saved = clienteRepository.save(existing);

        if (wasInactive && saved.isAtivo()) {
            mailService.sendMail(saved.getEmail(), "BANTADS - Conta aprovada!",
                    "Sua conta foi aprovada!\nVocê já pode entrar no BANTADS usando seu email e senha!");
        }

        return toResponseDTO(saved);
    }

    // --- Métodos de processamento para consumidores RabbitMQ ---

    public void processaNovoClienteEvent(ClienteDTO dto) {
        try {
            ClienteModel model = toModel(dto);
            model.setSenha(SecurityUtils.hash(dto.getSenha()));

            ClienteModel saved = clienteRepository.save(model);

            mailService.sendMail(saved.getEmail(), "BANTADS - Conta criada com sucesso!",
                    "Sua conta foi criada com sucesso!! Aguarde a aprovação de um gerente.");

            System.out.println("Salvo (" + saved.getNome() + ") via RabbitMQ");

            if (dto.getSagaId() != null && !dto.getSagaId().isBlank()) {
                // Fluxo orquestrado pelo ms-saga: notifica o orquestrador para avançar
                publicarEventoSagaClienteCriado(dto.getSagaId(), saved.getUuid().toString());
            } else {
                // Fluxo legado (coreografia): envia diretamente para autenticação
                UsuarioDTO uAuth = new UsuarioDTO(
                        saved.getUuid().toString(),
                        saved.getEmail(),
                        dto.getSenha(),
                        "CLIENTE",
                        false
                );
                rabbitTemplate.convertAndSend(RabbitMQConfig.FILA_AUTENTICACAO, uAuth);
            }
        } catch (Exception e) {
            System.err.println("Erro ao salvar cliente via RabbitMQ: " + e.getMessage());
            String errorId = dto.getResolvedUuid() != null ? dto.getResolvedUuid().toString() : "null";

            if (dto.getSagaId() != null && !dto.getSagaId().isBlank()) {
                publicarEventoSagaClienteErro(dto.getSagaId(), e.getMessage());
            } else {
                rabbitTemplate.convertAndSend(RabbitMQConfig.FILA_ERRO_NOVO_CLIENTE, errorId);
                rabbitTemplate.convertAndSend(RabbitMQConfig.FILA_ERRO_NOVO_CLIENTE_AUTENTICACAO, errorId);
            }

            if (dto.getEmail() != null) {
                mailService.sendMail(dto.getEmail(), "BANTADS - Não foi possível criar sua conta!",
                        "Não foi possível criar sua conta!!");
            }
        }
    }

    private void publicarEventoSagaClienteCriado(String sagaId, String uuidCliente) {
        try {
            String json = String.format(
                    "{\"sagaId\":\"%s\",\"sucesso\":true,\"uuidCliente\":\"%s\"}", sagaId, uuidCliente);
            rabbitTemplate.convertAndSend(RabbitMQConfig.SAGA_EVT_CLIENTE_CRIADO, json);
        } catch (Exception e) {
            System.err.println("Falha ao publicar SAGA_EVT_CLIENTE_CRIADO: " + e.getMessage());
        }
    }

    private void publicarEventoSagaClienteErro(String sagaId, String motivo) {
        try {
            String msg = motivo != null ? motivo.replace("\"", "'") : "erro no ms-cliente";
            String json = String.format(
                    "{\"sagaId\":\"%s\",\"sucesso\":false,\"mensagem\":\"%s\"}", sagaId, msg);
            rabbitTemplate.convertAndSend(RabbitMQConfig.SAGA_EVT_CLIENTE_ERRO, json);
        } catch (Exception e) {
            System.err.println("Falha ao publicar SAGA_EVT_CLIENTE_ERRO: " + e.getMessage());
        }
    }

    public void excluirClientePorUuid(String uuidStr) {
        try {
            UUID uuid = UUID.fromString(uuidStr);
            clienteRepository.findById(uuid).ifPresent(c -> {
                clienteRepository.deleteById(uuid);
                System.out.println("Compensação saga: cliente " + uuid + " excluído");
            });
        } catch (Exception e) {
            System.err.println("Erro na compensação de cliente: " + e.getMessage());
        }
    }

    public void processaUpdateClienteEvent(ClienteDTO dto) {
        UUID uuid = dto.getResolvedUuid();
        if (uuid == null) {
            System.err.println("UUID não fornecido para atualização do cliente.");
            return;
        }

        try {
            ClienteModel existing = clienteRepository.findById(uuid)
                    .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado!"));

            existing.setNome(dto.getNome());
            if (dto.getSenha() != null && !dto.getSenha().isBlank()) {
                existing.setSenha(SecurityUtils.hash(dto.getSenha()));
            }
            existing.setTelefone(dto.getTelefone());
            if (dto.getSalario() != null) {
                existing.setSalario(dto.getSalario());
            }
            existing.setEndereco(dto.getResolvedEndereco());
            existing.setCep(dto.getCep());
            existing.setCidade(dto.getCidadeAsString());
            existing.setEstado(dto.getEstadoAsString());
            existing.setAtivo(dto.isAtivo());

            ClienteModel saved = clienteRepository.save(existing);

            UsuarioDTO uAuth = new UsuarioDTO(
                    saved.getUuid().toString(),
                    saved.getEmail(),
                    dto.getSenha(), // Senha em texto puro enviada no evento
                    "CLIENTE",
                    true
            );

            rabbitTemplate.convertAndSend(RabbitMQConfig.FILA_AUTENTICACAO, uAuth);

            mailService.sendMail(saved.getEmail(), "BANTADS - Conta atualizada com sucesso!",
                    "Sua conta foi atualizada com sucesso!!");

            System.out.println("Atualizado (" + saved.getNome() + ") via RabbitMQ");
        } catch (Exception e) {
            System.err.println("Erro ao atualizar cliente via RabbitMQ: " + e.getMessage());
            // Envia o estado atual ou uuid para a fila de erro
            rabbitTemplate.convertAndSend(RabbitMQConfig.FILA_ERRO_UPDATE_CLIENTE, uuid.toString());
        }
    }

    public void processaNotificacaoUpdateContaEvent(NotificacaoDTO notificacao) {
        // Encontra o usuário a partir do idUsuario do tipo Long
        UUID uuid = new UUID(0L, notificacao.getIdUsuario());
        ClienteModel cliente = clienteRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado com ID: " + notificacao.getIdUsuario()));

        if (notificacao.isStatus()) {
            mailService.sendMail(cliente.getEmail(), "BANTADS - Seja bem vindo!",
                    "Sua conta foi analisada e aceita por nossa equipe. Acesse sua conta com a senha: "
                            + cliente.getSenha() + "!");
        } else {
            mailService.sendMail(cliente.getEmail(), "BANTADS - Conta recusada!",
                    "Sua conta foi analisada e recusada por nossa equipe. Motivo: "
                            + notificacao.getMessage() + "!");
        }
    }

    // --- Mapeamentos manuais limpos ---

    private ClienteModel toModel(ClienteDTO dto) {
        return ClienteModel.builder()
                .uuid(dto.getResolvedUuid())
                .nome(dto.getNome())
                .email(dto.getEmail())
                .cpf(dto.getCpf())
                .telefone(dto.getTelefone())
                .salario(dto.getSalario() != null ? dto.getSalario() : BigDecimal.ZERO)
                .endereco(dto.getResolvedEndereco())
                .cep(dto.getCep())
                .cidade(dto.getCidadeAsString())
                .estado(dto.getEstadoAsString())
                .senha(dto.getSenha())
                .ativo(dto.isAtivo())
                .cargo(dto.getCargo() != null ? dto.getCargo() : "CLIENTE")
                .build();
    }

    private ClienteResponseDTO toResponseDTO(ClienteModel model) {
        return new ClienteResponseDTO(
                model.getUuid(),
                model.getNome(),
                model.getEmail(),
                model.getCpf(),
                model.getSalario(),
                model.getEndereco(),
                model.getCep(),
                model.getCidade(),
                model.getEstado()
        );
    }
}
