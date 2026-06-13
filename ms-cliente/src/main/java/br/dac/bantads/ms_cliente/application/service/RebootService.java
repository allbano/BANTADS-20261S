package br.dac.bantads.ms_cliente.application.service;

import br.dac.bantads.ms_cliente.domain.model.ClienteModel;
import br.dac.bantads.ms_cliente.infrastructure.persistence.SpringDataClienteRepository;
import br.dac.bantads.ms_cliente.infrastructure.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Recarrega a base pré-cadastrada de clientes (enunciado BANTADS).
 *
 * Os UUIDs abaixo são UUIDv7 fixos (determinísticos por serem constantes) e
 * DEVEM permanecer idênticos aos referenciados em {@code conta_cliente_uuid} no
 * seed do ms-conta — contrato de consistência cruzada por UUID. Todos os clientes
 * pré-cadastrados já estão aprovados ({@code ativo=true}). Persistência via JPA puro.
 */
@Service
public class RebootService {

    // ── UUIDv7 fixos dos clientes (manter idêntico no ms-conta) ──
    public static final UUID UUID_CLI1 = UUID.fromString("01900000-0000-7000-8000-000000000c01"); // Catharyna
    public static final UUID UUID_CLI2 = UUID.fromString("01900000-0000-7000-8000-000000000c02"); // Cleuddônio
    public static final UUID UUID_CLI3 = UUID.fromString("01900000-0000-7000-8000-000000000c03"); // Catianna
    public static final UUID UUID_CLI4 = UUID.fromString("01900000-0000-7000-8000-000000000c04"); // Cutardo
    public static final UUID UUID_CLI5 = UUID.fromString("01900000-0000-7000-8000-000000000c05"); // Coândrya

    private final SpringDataClienteRepository repository;

    public RebootService(SpringDataClienteRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public List<ClienteModel> reboot() {
        repository.deleteAll();
        String senha = SecurityUtils.hash("tads");

        List<ClienteModel> clientes = List.of(
            ClienteModel.builder()
                .uuid(UUID_CLI1).nome("Catharyna").email("cli1@bantads.com.br")
                .cpf("12912861012").salario(new BigDecimal("10000.00"))
                .endereco("Rua das Flores, 100").cidade("Curitiba").estado("PR").cep("80000-000")
                .telefone("(41) 98888-1111").senha(senha).ativo(true).cargo("CLIENTE").build(),
            ClienteModel.builder()
                .uuid(UUID_CLI2).nome("Cleuddônio").email("cli2@bantads.com.br")
                .cpf("09506382000").salario(new BigDecimal("20000.00"))
                .endereco("Av. Brasil, 200").cidade("Curitiba").estado("PR").cep("80010-000")
                .telefone("(41) 98888-2222").senha(senha).ativo(true).cargo("CLIENTE").build(),
            ClienteModel.builder()
                .uuid(UUID_CLI3).nome("Catianna").email("cli3@bantads.com.br")
                .cpf("85733854057").salario(new BigDecimal("3000.00"))
                .endereco("Rua XV de Novembro, 300").cidade("Curitiba").estado("PR").cep("80020-000")
                .telefone("(41) 98888-3333").senha(senha).ativo(true).cargo("CLIENTE").build(),
            ClienteModel.builder()
                .uuid(UUID_CLI4).nome("Cutardo").email("cli4@bantads.com.br")
                .cpf("58872160006").salario(new BigDecimal("500.00"))
                .endereco("Rua Marechal Deodoro, 400").cidade("Curitiba").estado("PR").cep("80030-000")
                .telefone("(41) 98888-4444").senha(senha).ativo(true).cargo("CLIENTE").build(),
            ClienteModel.builder()
                .uuid(UUID_CLI5).nome("Coândrya").email("cli5@bantads.com.br")
                .cpf("76179646090").salario(new BigDecimal("1500.00"))
                .endereco("Av. Sete de Setembro, 500").cidade("Curitiba").estado("PR").cep("80040-000")
                .telefone("(41) 98888-5555").senha(senha).ativo(true).cargo("CLIENTE").build()
        );

        repository.saveAll(clientes);
        return clientes;
    }
}
