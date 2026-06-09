package br.dac.bantads.ms_conta.service;

import br.dac.bantads.ms_conta.model.ContaModel;
import br.dac.bantads.ms_conta.model.ContaView;
import br.dac.bantads.ms_conta.model.event.ContaAtualizadaEvent;
import br.dac.bantads.ms_conta.repository.ContaViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * CQRS read-side updater.
 * Escuta ContaAtualizadaEvent (publicado pelo ContaService após cada escrita)
 * e mantém o ContaView sincronizado com o estado atual da conta.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ContaEventHandler {

    private final ContaViewRepository contaViewRepository;

    @EventListener
    public void onContaAtualizada(ContaAtualizadaEvent event) {
        ContaModel conta = event.conta();
        ContaView view = ContaView.builder()
                .uuidConta(conta.getUuidConta())
                .uuidCliente(conta.getUuidCliente())
                .numero(conta.getNumero())
                .dataCriacao(conta.getDataCriacao())
                .saldo(conta.getSaldo())
                .limite(conta.getLimite())
                .uuidGerente(conta.getUuidGerente())
                .ativo(conta.isAtivo())
                .rejeitadoMotivo(conta.getRejeitadoMotivo())
                .rejeitadoData(conta.getRejeitadoData())
                .build();
        contaViewRepository.save(view);
        log.debug("ContaView atualizada para conta {}", conta.getUuidConta());
    }

    public void onContaExcluida(java.util.UUID uuidCliente) {
        contaViewRepository.findByUuidCliente(uuidCliente)
                .ifPresent(v -> {
                    contaViewRepository.delete(v);
                    log.debug("ContaView excluída para cliente {}", uuidCliente);
                });
    }
}
