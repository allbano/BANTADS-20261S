package br.dac.bantads.ms_conta.repository.read;

import br.dac.bantads.ms_conta.model.read.MovimentacaoView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repositório do modelo de leitura das movimentações (CQRS read side).
 * Utilizado exclusivamente pelas consultas (extrato R8, listagem) — nunca pelo
 * lado de Comando, que escreve em MovimentacaoModel (conta_cud).
 */
public interface MovimentacaoViewRepository extends JpaRepository<MovimentacaoView, UUID> {

    List<MovimentacaoView> findByUuidContaOrderByDataHoraAsc(UUID uuidConta);

    List<MovimentacaoView> findByUuidContaAndDataHoraBetweenOrderByDataHoraAsc(
            UUID uuidConta,
            LocalDateTime dataInicio,
            LocalDateTime dataFim
    );
}
