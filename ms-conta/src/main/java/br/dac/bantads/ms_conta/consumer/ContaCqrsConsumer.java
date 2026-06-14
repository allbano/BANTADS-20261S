package br.dac.bantads.ms_conta.consumer;

import br.dac.bantads.ms_conta.config.RabbitMQConfig;
import br.dac.bantads.ms_conta.dto.ContaSyncDTO;
import br.dac.bantads.ms_conta.model.read.ContaView;
import br.dac.bantads.ms_conta.repository.read.ContaViewRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Consumidor do eixo CQRS do MS Conta (lado Consulta).
 *
 * Recebe os eventos publicados pelo {@link br.dac.bantads.ms_conta.service.CqrsPublisher}
 * e mantem a projecao {@link ContaView} (banco de leitura conta_r) sincronizada
 * com o estado do banco de Comando (conta_cud). As operacoes de escrita usam o
 * {@code readTransactionManager} via {@link ContaViewRepository}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ContaCqrsConsumer {

    private final ContaViewRepository contaViewRepository;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_CONTA_CQRS_ATUALIZADA)
    public void onContaAtualizada(String msg) throws Exception {
        ContaSyncDTO dto = objectMapper.readValue(msg, ContaSyncDTO.class);
        ContaView view = ContaView.builder()
                .uuidConta(dto.uuidConta())
                .uuidCliente(dto.uuidCliente())
                .clienteCpf(dto.clienteCpf())
                .numero(dto.numero())
                .dataCriacao(dto.dataCriacao())
                .saldo(dto.saldo())
                .limite(dto.limite())
                .uuidGerente(dto.uuidGerente())
                .ativo(dto.ativo())
                .rejeitadoMotivo(dto.rejeitadoMotivo())
                .rejeitadoData(dto.rejeitadoData())
                .build();
        contaViewRepository.save(view);
        log.debug("ContaView (conta_r) sincronizada para conta {}", dto.uuidConta());
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_CONTA_CQRS_EXCLUIDA)
    public void onContaExcluida(String uuidClienteStr) {
        UUID uuidCliente = UUID.fromString(uuidClienteStr.replace("\"", "").trim());
        contaViewRepository.findByUuidCliente(uuidCliente).ifPresent(v -> {
            contaViewRepository.delete(v);
            log.debug("ContaView (conta_r) removida para cliente {}", uuidCliente);
        });
    }
}
