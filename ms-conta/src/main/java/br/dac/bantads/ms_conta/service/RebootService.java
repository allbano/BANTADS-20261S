package br.dac.bantads.ms_conta.service;

import br.dac.bantads.ms_conta.model.cud.ContaModel;
import br.dac.bantads.ms_conta.model.cud.MovimentacaoModel;
import br.dac.bantads.ms_conta.model.enums.TipoMovimentacao;
import br.dac.bantads.ms_conta.repository.cud.ContaRepository;
import br.dac.bantads.ms_conta.repository.cud.MovimentacaoRepository;
import br.dac.bantads.ms_conta.repository.read.ContaViewRepository;
import br.dac.bantads.ms_conta.repository.read.MovimentacaoViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Recarrega a base pré-cadastrada de contas e movimentações (enunciado BANTADS).
 *
 * Os UUIDv7 de cliente e gerente são FIXOS e devem bater exatamente com os seeds
 * do ms-cliente ({@code conta_cliente_uuid}) e ms-funcionario ({@code conta_gerente_uuid}):
 * é o contrato de consistência cruzada do seed por UUID. Após escrever no banco de
 * Comando (conta_cud), publica a sincronização CQRS para projetar em conta_r.
 * Persistência 100% via JPA/Hibernate (sem SQL nativo).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RebootService {

    // ── UUIDv7 fixos dos clientes (idêntico ao ms-cliente) ──
    private static final UUID CLI1 = UUID.fromString("01900000-0000-7000-8000-000000000c01"); // Catharyna
    private static final UUID CLI2 = UUID.fromString("01900000-0000-7000-8000-000000000c02"); // Cleuddônio
    private static final UUID CLI3 = UUID.fromString("01900000-0000-7000-8000-000000000c03"); // Catianna
    private static final UUID CLI4 = UUID.fromString("01900000-0000-7000-8000-000000000c04"); // Cutardo
    private static final UUID CLI5 = UUID.fromString("01900000-0000-7000-8000-000000000c05"); // Coândrya

    // ── UUIDv7 fixos dos gerentes (idêntico ao ms-funcionario) ──
    private static final UUID GER1 = UUID.fromString("01900000-0000-7000-8000-000000000a01"); // Geniéve
    private static final UUID GER2 = UUID.fromString("01900000-0000-7000-8000-000000000a02"); // Godophredo
    private static final UUID GER3 = UUID.fromString("01900000-0000-7000-8000-000000000a03"); // Gyândula

    // ── UUIDv7 fixos das contas (o sufixo codifica o número da conta) ──
    private static final UUID CT_1291 = UUID.fromString("01900000-0000-7000-8000-000000001291");
    private static final UUID CT_0950 = UUID.fromString("01900000-0000-7000-8000-000000000950");
    private static final UUID CT_8573 = UUID.fromString("01900000-0000-7000-8000-000000008573");
    private static final UUID CT_5887 = UUID.fromString("01900000-0000-7000-8000-000000005887");
    private static final UUID CT_7617 = UUID.fromString("01900000-0000-7000-8000-000000007617");

    private final ContaRepository contaRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final ContaViewRepository contaViewRepository;
    private final MovimentacaoViewRepository movimentacaoViewRepository;
    private final CqrsPublisher cqrsPublisher;

    @Transactional
    public List<ContaModel> reboot() {
        // Limpa o banco de Comando (filho antes do pai) e a projeção de Consulta.
        // deleteAllInBatch: bulk DELETE sem carregar entidades no contexto — evita
        // conflito de merge com os UUIDs fixos ao re-semear (ObjectNotFoundException).
        movimentacaoRepository.deleteAllInBatch();
        contaRepository.deleteAllInBatch();
        contaViewRepository.deleteAllInBatch();
        movimentacaoViewRepository.deleteAllInBatch();

        ContaModel c1291 = salvarConta(CT_1291, CLI1, "12912861012", GER1, "1291", LocalDate.of(2000, 1, 1),  "800.00",    "5000.00");
        ContaModel c0950 = salvarConta(CT_0950, CLI2, "09506382000", GER2, "0950", LocalDate.of(1990, 10, 10), "-10000.00", "10000.00");
        ContaModel c8573 = salvarConta(CT_8573, CLI3, "85733854057", GER3, "8573", LocalDate.of(2012, 12, 12), "-1000.00",  "1500.00");
        ContaModel c5887 = salvarConta(CT_5887, CLI4, "58872160006", GER1, "5887", LocalDate.of(2022, 2, 22),  "150000.00", "0.00");
        ContaModel c7617 = salvarConta(CT_7617, CLI5, "76179646090", GER2, "7617", LocalDate.of(2025, 1, 1),   "1500.00",   "0.00");

        // ── 15 movimentações pré-cadastradas ──
        // Catharyna (1291)
        mov(c1291, TipoMovimentacao.DEPOSITO, "1000.00", LocalDateTime.of(2020, 1, 1, 10, 0), null);
        mov(c1291, TipoMovimentacao.DEPOSITO, "900.00",  LocalDateTime.of(2020, 1, 1, 11, 0), null);
        mov(c1291, TipoMovimentacao.SAQUE,    "550.00",  LocalDateTime.of(2020, 1, 1, 12, 0), null);
        mov(c1291, TipoMovimentacao.SAQUE,    "350.00",  LocalDateTime.of(2020, 1, 1, 13, 0), null);
        mov(c1291, TipoMovimentacao.DEPOSITO, "2000.00", LocalDateTime.of(2020, 1, 10, 10, 0), null);
        mov(c1291, TipoMovimentacao.SAQUE,    "500.00",  LocalDateTime.of(2020, 1, 15, 10, 0), null);
        mov(c1291, TipoMovimentacao.TRANSFERENCIA, "1700.00", LocalDateTime.of(2020, 1, 20, 10, 0), c0950); // → Cleuddônio
        // Cleuddônio (0950)
        mov(c0950, TipoMovimentacao.DEPOSITO, "1000.00", LocalDateTime.of(2025, 1, 1, 10, 0), null);
        mov(c0950, TipoMovimentacao.DEPOSITO, "5000.00", LocalDateTime.of(2025, 1, 2, 10, 0), null);
        mov(c0950, TipoMovimentacao.SAQUE,    "200.00",  LocalDateTime.of(2025, 1, 10, 10, 0), null);
        mov(c0950, TipoMovimentacao.DEPOSITO, "7000.00", LocalDateTime.of(2025, 2, 5, 10, 0), null);
        // Catianna (8573)
        mov(c8573, TipoMovimentacao.DEPOSITO, "1000.00", LocalDateTime.of(2025, 5, 5, 10, 0), null);
        mov(c8573, TipoMovimentacao.SAQUE,    "2000.00", LocalDateTime.of(2025, 5, 6, 10, 0), null);
        // Cutardo (5887)
        mov(c5887, TipoMovimentacao.DEPOSITO, "150000.00", LocalDateTime.of(2025, 6, 1, 10, 0), null);
        // Coândrya (7617)
        mov(c7617, TipoMovimentacao.DEPOSITO, "1500.00", LocalDateTime.of(2025, 7, 1, 10, 0), null);

        // Sincroniza o lado de Consulta (conta_r) via mensageria CQRS.
        List<ContaModel> contas = List.of(c1291, c0950, c8573, c5887, c7617);
        contas.forEach(cqrsPublisher::publicarConta);

        log.info("Reboot ms-conta concluído: {} contas e 15 movimentações recarregadas.", contas.size());
        return contas;
    }

    private ContaModel salvarConta(UUID contaUuid, UUID clienteUuid, String clienteCpf, UUID gerenteUuid,
                                   String numero, LocalDate criacao, String saldo, String limite) {
        ContaModel conta = ContaModel.builder()
                .uuidConta(contaUuid)
                .uuidCliente(clienteUuid)
                .clienteCpf(clienteCpf)
                .uuidGerente(gerenteUuid)
                .numero(numero)
                .dataCriacao(criacao)
                .saldo(new BigDecimal(saldo))
                .limite(new BigDecimal(limite))
                .ativo(true)
                .movimentacoes(new ArrayList<>())
                .build();
        return contaRepository.save(conta);
    }

    private void mov(ContaModel conta, TipoMovimentacao tipo, String valor,
                     LocalDateTime dataHora, ContaModel contaDestino) {
        MovimentacaoModel m = MovimentacaoModel.builder()
                .conta(conta)
                .tipo(tipo)
                .valor(new BigDecimal(valor))
                .dataHora(dataHora)
                .uuidContaDestino(contaDestino != null ? contaDestino.getUuidConta() : null)
                .build();
        MovimentacaoModel saved = movimentacaoRepository.save(m);
        // Replica a movimentação de seed para o banco de leitura (conta_r).
        cqrsPublisher.publicarMovimentacao(saved, contaDestino != null ? contaDestino.getNumero() : null);
    }
}
