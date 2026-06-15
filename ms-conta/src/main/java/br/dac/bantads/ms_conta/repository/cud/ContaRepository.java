package br.dac.bantads.ms_conta.repository.cud;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.dac.bantads.ms_conta.model.cud.ContaModel;

/**
 * Repositório JPA para a entidade ContaModel.
 *
 * Métodos customizados atendem aos seguintes requisitos:
 * - buscarPorNumero: localizar conta destino em transferências (R7)
 * - buscarPorCliente: garantir unicidade de conta por cliente (R1)
 * - buscarPorGerente: listar clientes de um gerente (R12)
 * - contarPorGerente: atribuir gerente com menos clientes (R1, R17, R18)
 * - buscarTop3PorSaldo: exibir os 3 melhores clientes (R14)
 */
public interface ContaRepository extends JpaRepository<ContaModel, UUID> {

    Optional<ContaModel> findByNumero(String numero);

    Optional<ContaModel> findByUuidCliente(UUID uuidCliente);

    List<ContaModel> findByUuidGerenteOrderByNumeroAsc(UUID uuidGerente);

    long countByUuidGerente(UUID uuidGerente);

    @Query("SELECT c.uuidGerente FROM ContaModel c GROUP BY c.uuidGerente ORDER BY COUNT(c) ASC")
    List<UUID> findGerentesOrdenadosPorMenosContas(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT c.uuidGerente FROM ContaModel c GROUP BY c.uuidGerente ORDER BY COUNT(c) DESC")
    List<UUID> findGerentesOrdenadosPorMaisContas(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT c.uuidGerente FROM ContaModel c WHERE c.uuidGerente != :currentGerente GROUP BY c.uuidGerente ORDER BY COUNT(c) ASC")
    List<UUID> findGerentesOrdenadosPorMenosContasExcluindo(@Param("currentGerente") UUID currentGerente, org.springframework.data.domain.Pageable pageable);

    List<ContaModel> findByUuidGerenteAndAtivo(UUID uuidGerente, boolean ativo);

    /**
     * Conta ativa mais recente do banco. UUIDv7 é temporal, então o maior UUID é a
     * conta criada por último (o cliente recém-aprovado) — usada na reatribuição ao
     * inserir um gerente (R17), de forma determinística.
     */
    Optional<ContaModel> findFirstByAtivoTrueOrderByUuidContaDesc();

    List<ContaModel> findByUuidGerenteOrderBySaldoDesc(UUID uuidGerente);

    @Query("SELECT c FROM ContaModel c ORDER BY c.saldo DESC LIMIT 3")
    List<ContaModel> findTop3ByOrderBySaldoDesc();

    @Query("SELECT COALESCE(SUM(c.saldo), 0) FROM ContaModel c "
         + "WHERE c.uuidGerente = :uuidGerente AND c.saldo >= 0")
    java.math.BigDecimal somarSaldosPositivosPorGerente(@Param("uuidGerente") UUID uuidGerente);

    @Query("SELECT COALESCE(SUM(c.saldo), 0) FROM ContaModel c "
         + "WHERE c.uuidGerente = :uuidGerente AND c.saldo < 0")
    java.math.BigDecimal somarSaldosNegativosPorGerente(@Param("uuidGerente") UUID uuidGerente);
}
