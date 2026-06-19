package br.dac.bantads.ms_cliente.application.service;

import br.dac.bantads.ms_cliente.application.dto.*;
import br.dac.bantads.ms_cliente.domain.model.ClienteModel;
import br.dac.bantads.ms_cliente.domain.repository.ClienteRepository;
import br.dac.bantads.ms_cliente.infrastructure.config.RabbitMQConfig;
import br.dac.bantads.ms_cliente.infrastructure.security.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
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
    private final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

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
            @Value("${services.conta:http://localhost:40006}") String contaServiceUrl) {
        this.clienteRepository = clienteRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.restClient = RestClient.builder().baseUrl(contaServiceUrl).build();
    }

    /**
     * Centralizacao do e-mail (R1/R10/R11): publica uma notificacao TIPADA no canal
     * unico {@code saga.cmd.notificar.cliente}. SOMENTE o ms_notificacao monta o
     * texto e fala com o SMTP. Sem {@code sagaId} → e-mail "solto" (nao espera reply
     * de SAGA). Publicacao nao-fatal: falha apenas loga, o fluxo continua.
     */
    private void publicarNotificacao(String tipo, String email, String nome, String motivo) {
        try {
            Map<String, Object> notif = new HashMap<>();
            notif.put("email", email);
            notif.put("nome", nome);
            notif.put("tipo", tipo);
            if (motivo != null) notif.put("motivo", motivo);
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "saga.cmd.notificar.cliente",
                    objectMapper.writeValueAsString(notif));
        } catch (Exception e) {
            System.err.println("Falha ao publicar notificacao (" + tipo + "): " + e.getMessage());
        }
    }

    // --- Métodos Existentes mantidos intactos ---

    public List<ClienteParaAprovarResponse> getClientesParaAprovar() {
        // "Aguardando aprovação" (R9) = status PENDENTE. O autocadastro cria o
        // cliente como PENDENTE (e já cria a conta); aprovar (R10) → APROVADO e
        // rejeitar (R11) → REJEITADO. O critério é o 'status', não a existência de
        // conta (toda conta nasce junto) nem o flag 'ativo' (pendente e rejeitado
        // são ambos inativos).
        return clienteRepository.findAll().stream()
                .filter(c -> "PENDENTE".equals(c.getStatus()))
                // UUIDv7 é temporal → ordena por ordem de criação (R9 espera autocad1 antes de autocad2).
                .sorted(Comparator.comparing(ClienteModel::getUuid))
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
        // Busca direta no repositório (usada na API Composition do /login pelo gateway).
        return clienteRepository.findByEmail(email).map(this::toResponseDTO);
    }

    /**
     * R11 — Rejeitar cliente. NÃO é SAGA: escrita local (marca inativo) + publica
     * o evento de notificação de rejeição. Idempotente o suficiente para o contexto.
     */
    public void rejeitar(String cpf, String motivo) {
        ClienteModel c = clienteRepository.findByCpf(cpf)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado!"));
        c.setAtivo(false);
        c.setStatus("REJEITADO"); // R11 — sai da fila de "aguardando aprovação"
        clienteRepository.save(c);
        // R11 — grava motivo/data da reprovação na conta (ms-conta + CQRS). Resiliente:
        // se o ms-conta estiver indisponível, a rejeição do cliente e o e-mail continuam.
        try {
            restClient.post()
                    .uri("/contas/cliente/{uuid}/rejeitar", c.getUuid())
                    .body(Map.of("motivo", motivo != null ? motivo : ""))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            System.err.println("Falha ao gravar reprovação no ms-conta: " + e.getMessage());
        }
        // R11 — e-mail de rejeição (com o motivo) montado e enviado pelo ms_notificacao.
        publicarNotificacao("REJEICAO", c.getEmail(), c.getNome(), motivo != null ? motivo : "");
    }

    // --- Métodos de processamento para consumidores RabbitMQ ---

    public void processaNovoClienteEvent(ClienteDTO dto) {
        // Falha rápida em CPF duplicado: a violação de unicidade só apareceria no
        // commit (fora deste try/catch), causando retry/DLQ e timeout da SAGA.
        if (dto.getCpf() != null && clienteRepository.findByCpf(dto.getCpf()).isPresent()) {
            System.err.println("CPF já cadastrado no autocadastro: " + dto.getCpf());
            if (dto.getSagaId() != null && !dto.getSagaId().isBlank()) {
                publicarEventoSagaClienteErro(dto.getSagaId(), "CPF já cadastrado");
            }
            return;
        }
        try {
            ClienteModel model = toModel(dto);
            model.setSenha(SecurityUtils.hash(dto.getSenha()));

            ClienteModel saved = clienteRepository.save(model);

            // R1 — confirma o recebimento da solicitacao de autocadastro (operacao assincrona).
            publicarNotificacao("CONTA_CRIADA", saved.getEmail(), saved.getNome(), null);

            System.out.println("Salvo (" + saved.getNome() + ") via RabbitMQ");

            // Fluxo orquestrado pelo ms-saga: notifica o orquestrador para avançar
            if (dto.getSagaId() != null && !dto.getSagaId().isBlank()) {
                publicarEventoSagaClienteCriado(dto.getSagaId(), saved.getUuid().toString());
            }
        } catch (Exception e) {
            System.err.println("Erro ao salvar cliente via RabbitMQ: " + e.getMessage());

            if (dto.getSagaId() != null && !dto.getSagaId().isBlank()) {
                publicarEventoSagaClienteErro(dto.getSagaId(), e.getMessage());
            }

            if (dto.getEmail() != null) {
                // R1 — em caso de falha interna, avisa o cliente que a solicitacao nao foi efetuada.
                publicarNotificacao("FALHA_AUTOCADASTRO", dto.getEmail(), dto.getNome(), null);
            }
        }
    }

    private void publicarEventoSagaClienteCriado(String sagaId, String uuidCliente) {
        try {
            String json = String.format(
                    "{\"sagaId\":\"%s\",\"sucesso\":true,\"uuidCliente\":\"%s\"}", sagaId, uuidCliente);
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.SAGA_EVT_CLIENTE_CRIADO, json);
        } catch (Exception e) {
            System.err.println("Falha ao publicar SAGA_EVT_CLIENTE_CRIADO: " + e.getMessage());
        }
    }

    private void publicarEventoSagaClienteErro(String sagaId, String motivo) {
        try {
            String msg = motivo != null ? motivo.replace("\"", "'") : "erro no ms-cliente";
            String json = String.format(
                    "{\"sagaId\":\"%s\",\"sucesso\":false,\"mensagem\":\"%s\"}", sagaId, msg);
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.SAGA_EVT_CLIENTE_ERRO, json);
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
                .status(dto.isAtivo() ? "APROVADO" : "PENDENTE")
                .build();
    }

    private ClienteResponseDTO toResponseDTO(ClienteModel model) {
        return new ClienteResponseDTO(
                model.getUuid(),
                model.getNome(),
                model.getEmail(),
                model.getCpf(),
                model.getTelefone(),
                model.getSalario(),
                model.getEndereco(),
                model.getCep(),
                model.getCidade(),
                model.getEstado()
        );
    }
}
