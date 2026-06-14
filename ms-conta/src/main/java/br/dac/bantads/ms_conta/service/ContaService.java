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
    public ContaModel updateConta(ContaRabbitDTO dto) {
        UUID clientUuid = dto.parseClientUuid();
        if (clientUuid == null) {
            throw new IllegalArgumentException("Cliente UUID inválido ou ausente");
        }

        ContaModel conta = contaRepository.findByUuidCliente(clientUuid)
                .orElseThrow(() -> new NoSuchElementException("Conta não encontrada para o cliente: " + clientUuid));

        if (dto.getSaldo() != null) {
            conta.setSaldo(dto.getSaldo());
        }
        if (dto.getSalario() != null) {
            conta.setLimite(calculateLimit(dto.getSalario()));
        }
        
        conta.setAtivo(dto.isAtivo());
        conta.setRejeitadoMotivo(dto.getRejeitadoMotivo());
        
        if (!dto.isAtivo() && dto.getRejeitadoMotivo() != null) {
            conta.setRejeitadoData(LocalDate.now());
        } else {
            conta.setRejeitadoData(null);
        }

        log.info("Atualizando conta do cliente {}: ativa={}, limite={}", clientUuid, conta.isAtivo(), conta.getLimite());
        ContaModel saved = contaRepository.save(conta);
        cqrsPublisher.publicarConta(saved);
        return saved;
    }

    @Transactional
    public void atribuiContaGerente(UUID newGerenteUuid) {
        log.info("Novo gerente registrado: {}. Iniciando reatribuição de conta para balanceamento.", newGerenteUuid);
        List<UUID> mostAccountsManagers = contaRepository.findGerentesOrdenadosPorMaisContas(PageRequest.of(0, 1));
        if (mostAccountsManagers.isEmpty()) {
            log.info("Nenhum gerente com contas encontrado para balanceamento.");
            return;
        }

        UUID busiestManager = mostAccountsManagers.get(0);
        if (busiestManager.equals(newGerenteUuid)) {
            log.info("Gerente mais atarefado é o próprio gerente novo. Nenhuma reatribuição necessária.");
            return;
        }

        List<ContaModel> accounts = contaRepository.findByUuidGerenteOrderByNumeroAsc(busiestManager);
        if (!accounts.isEmpty()) {
            ContaModel accountToReassign = accounts.get(0);
            accountToReassign.setUuidGerente(newGerenteUuid);
            ContaModel saved = contaRepository.save(accountToReassign);
            cqrsPublisher.publicarConta(saved);
            log.info("Conta {} reatribuída do gerente {} para o gerente {}", accountToReassign.getUuidConta(), busiestManager, newGerenteUuid);
        }
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
