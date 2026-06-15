package br.dac.bantads.ms_conta.repository.read;

import br.dac.bantads.ms_conta.model.read.ContaView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório do modelo de leitura (CQRS read side).
 * Utilizado exclusivamente pelo ContaQueryService — nunca pelo ContaService (write side).
 */
public interface ContaViewRepository extends JpaRepository<ContaView, UUID> {

    Optional<ContaView> findByUuidCliente(UUID uuidCliente);

    Optional<ContaView> findByNumero(String numero);

    List<ContaView> findByUuidGerenteOrderByNumeroAsc(UUID uuidGerente);

    List<ContaView> findByUuidGerenteAndAtivo(UUID uuidGerente, boolean ativo);

    List<ContaView> findByUuidGerenteOrderBySaldoDesc(UUID uuidGerente);

    @Query("SELECT v FROM ContaView v ORDER BY v.saldo DESC LIMIT 3")
    List<ContaView> findTop3ByOrderBySaldoDesc();

    @Query("SELECT COALESCE(SUM(v.saldo), 0) FROM ContaView v "
         + "WHERE v.uuidGerente = :uuidGerente AND v.saldo >= 0")
    java.math.BigDecimal somarSaldosPositivosPorGerente(@Param("uuidGerente") UUID uuidGerente);

    @Query("SELECT COALESCE(SUM(v.saldo), 0) FROM ContaView v "
         + "WHERE v.uuidGerente = :uuidGerente AND v.saldo < 0")
    java.math.BigDecimal somarSaldosNegativosPorGerente(@Param("uuidGerente") UUID uuidGerente);
}
