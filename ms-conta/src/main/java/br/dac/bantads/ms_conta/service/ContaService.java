package br.dac.bantads.ms_conta.service;

import br.dac.bantads.ms_conta.dto.ContaRabbitDTO;
import br.dac.bantads.ms_conta.model.cud.ContaModel;
import br.dac.bantads.ms_conta.repository.cud.ContaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContaService {

    private final ContaRepository contaRepository;
    private final CqrsPublisher cqrsPublisher;
    private final Random random = new Random();

    public BigDecimal calculateLimit(BigDecimal salario) {
        if (salario != null && salario.compareTo(new BigDecimal("2000.00")) >= 0) {
            return salario.divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    private String generateUniqueAccountNumber() {
        while (true) {
            String number = String.format("%04d", random.nextInt(10000));
            if (contaRepository.findByNumero(number).isEmpty()) {
                return number;
            }
        }
    }

    @Transactional
    public ContaModel registrarConta(ContaRabbitDTO dto) {
        UUID clientUuid = dto.parseClientUuid();
        if (clientUuid == null) {
            log.error("Tentativa de criar conta com UUID de cliente inválido: {}", dto.getUuidCliente());
            throw new IllegalArgumentException("Cliente UUID inválido ou ausente");
        }

        Optional<ContaModel> existing = contaRepository.findByUuidCliente(clientUuid);
        if (existing.isPresent()) {
            log.info("Conta já existe para o cliente: {}", clientUuid);
            return existing.get();
        }

        UUID managerUuid = dto.parseGerenteUuid();
        if (managerUuid == null) {
            List<UUID> managers = contaRepository.findGerentesOrdenadosPorMenosContas(PageRequest.of(0, 1));
            if (!managers.isEmpty()) {
                managerUuid = managers.get(0);
            } else {
                // Fallback: generate random UUID if there are no accounts in the database yet
                managerUuid = UUID.randomUUID();
                log.warn("Nenhum gerente encontrado no banco para atribuição de conta. Gerando UUID aleatório para o gerente: {}", managerUuid);
            }
        }

        BigDecimal salario = dto.getSalario() != null ? dto.getSalario() : BigDecimal.ZERO;
        BigDecimal limite = calculateLimit(salario);

        ContaModel conta = ContaModel.builder()
                .uuidCliente(clientUuid)
                .clienteCpf(dto.getCpf())
                .numero(generateUniqueAccountNumber())
                .dataCriacao(LocalDate.now())
                .saldo(dto.getSaldo() != null ? dto.getSaldo() : BigDecimal.ZERO)
                .limite(limite)
                .uuidGerente(managerUuid)
                .ativo(false) // starts inactive (pending)
                .build();

        log.info("Salvando nova conta para o cliente {}, sob a gerência do gerente {}", clientUuid, managerUuid);
        ContaModel saved = contaRepository.save(conta);
        cqrsPublisher.publicarConta(saved);
        return saved;
    }

    /** R10 — ativa a conta (aprovação) garantindo o limite pela regra do salário. */
    @Transactional
    public String ativarConta(UUID uuidCliente, BigDecimal salario) {
        ContaModel conta = contaRepository.findByUuidCliente(uuidCliente)
                .orElseThrow(() -> new NoSuchElementException("Conta não encontrada para o cliente: " + uuidCliente));
        conta.setAtivo(true);
        if (salario != null) {
            conta.setLimite(calculateLimit(salario));
        }
        ContaModel saved = contaRepository.save(conta);
        cqrsPublisher.publicarConta(saved);
        return saved.getNumero();
    }

    /** Compensação de R10 — desativa a conta. */
    @Transactional
    public void desativarConta(UUID uuidCliente) {
        contaRepository.findByUuidCliente(uuidCliente).ifPresent(conta -> {
            conta.setAtivo(false);
            cqrsPublisher.publicarConta(contaRepository.save(conta));
        });
    }

    /**
     * R4 — recalcula o limite a partir do novo salário. Regra: se o novo limite
     * ficar abaixo do saldo negativo da conta, o limite passa a ser |saldo negativo|.
     */
    @Transactional
    public void recalcularLimite(UUID uuidCliente, BigDecimal salario) {
        ContaModel conta = contaRepository.findByUuidCliente(uuidCliente)
                .orElseThrow(() -> new NoSuchElementException("Conta não encontrada para o cliente: " + uuidCliente));
        BigDecimal novoLimite = calculateLimit(salario);
        if (conta.getSaldo() != null && conta.getSaldo().signum() < 0) {
            BigDecimal saldoAbs = conta.getSaldo().abs();
            if (novoLimite.compareTo(saldoAbs) < 0) {
                novoLimite = saldoAbs;
            }
        }
        conta.setLimite(novoLimite);
        cqrsPublisher.publicarConta(contaRepository.save(conta));
    }

    @Transactional
    public void excluirContaPorCliente(UUID clientUuid) {
        contaRepository.findByUuidCliente(clientUuid).ifPresent(conta -> {
            log.info("Excluindo conta do cliente: {}", clientUuid);
            contaRepository.delete(conta);
            cqrsPublisher.publicarExclusao(clientUuid);
        });
    }

    @Transactional
    public void atribuiContaGerente(UUID newGerenteUuid) {
        log.info("Novo gerente registrado: {}. Reatribuindo a conta ativa mais recente para balanceamento.", newGerenteUuid);
        // Move a conta ATIVA mais recente do banco (o cliente recém-aprovado) ao novo
        // gerente. Determinístico (UUIDv7 temporal) e garante que o novo gerente receba
        // um cliente ativo — não a conta inativa de um cliente rejeitado (R17).
        Optional<ContaModel> recente = contaRepository.findFirstByAtivoTrueOrderByUuidContaDesc();
        if (recente.isEmpty()) {
            log.info("Nenhuma conta ativa encontrada para balanceamento.");
            return;
        }
        ContaModel conta = recente.get();
        if (newGerenteUuid.equals(conta.getUuidGerente())) {
            log.info("Conta mais recente já é do gerente novo. Nenhuma reatribuição necessária.");
            return;
        }
        UUID anterior = conta.getUuidGerente();
        conta.setUuidGerente(newGerenteUuid);
        ContaModel saved = contaRepository.save(conta);
        cqrsPublisher.publicarConta(saved);
        log.info("Conta {} reatribuída do gerente {} para o gerente {}", conta.getUuidConta(), anterior, newGerenteUuid);
    }

    @Transactional
    public void distribuiContasGerente(UUID deletedGerenteUuid) {
        log.info("Gerente excluído: {}. Iniciando redistribuição de suas contas.", deletedGerenteUuid);
        List<ContaModel> accountsToRedistribute = contaRepository.findByUuidGerenteOrderByNumeroAsc(deletedGerenteUuid);
        if (accountsToRedistribute.isEmpty()) {
            log.info("Nenhuma conta para redistribuir para o gerente {}", deletedGerenteUuid);
            return;
        }

        for (ContaModel account : accountsToRedistribute) {
            List<UUID> managers = contaRepository.findGerentesOrdenadosPorMenosContasExcluindo(deletedGerenteUuid, PageRequest.of(0, 1));
            if (!managers.isEmpty()) {
                UUID recipientManager = managers.get(0);
                account.setUuidGerente(recipientManager);
                ContaModel saved = contaRepository.save(account);
                cqrsPublisher.publicarConta(saved);
                log.info("Conta {} do gerente excluído foi atribuída ao gerente {}", account.getUuidConta(), recipientManager);
            } else {
                log.warn("Nenhum outro gerente disponível para receber a conta {}", account.getUuidConta());
            }
        }
    }
}
