package br.dac.bantads.ms_cliente.application.service;

import br.dac.bantads.ms_cliente.application.dto.ClienteParaAprovarResponse;
import br.dac.bantads.ms_cliente.application.dto.ClienteResponse;
import br.dac.bantads.ms_cliente.domain.model.ClienteModel;
import br.dac.bantads.ms_cliente.domain.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final RestClient restClient;

    public record ContaResponseDTO(
            UUID uuidConta,
            UUID uuidCliente,
            String numero,
            LocalDate dataCriacao,
            BigDecimal saldo,
            BigDecimal limite,
            UUID uuidGerente
    ) {}

    public ClienteService(ClienteRepository clienteRepository, @Value("${services.conta:http://localhost:40006}") String contaServiceUrl) {
        this.clienteRepository = clienteRepository;
        this.restClient = RestClient.builder().baseUrl(contaServiceUrl).build();
    }

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
}
