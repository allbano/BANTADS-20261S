package br.dac.bantads.ms_conta.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.dac.bantads.ms_conta.model.MovimentacaoModel;

/**
 * Repositório JPA para a entidade MovimentacaoModel.
 *
 * Consulta de extrato (R8): o cliente informa data de início e data de fim,
 * o sistema retorna todas as movimentações daquele intervalo ordenadas
 * cronologicamente para montagem do extrato com saldo consolidado diário.
 */
public interface MovimentacaoRepository extends JpaRepository<MovimentacaoModel, UUID> {

    List<MovimentacaoModel> findByConta_UuidContaAndDataHoraBetweenOrderByDataHoraAsc(
            UUID uuidConta,
            LocalDateTime dataInicio,
            LocalDateTime dataFim
    );
}
