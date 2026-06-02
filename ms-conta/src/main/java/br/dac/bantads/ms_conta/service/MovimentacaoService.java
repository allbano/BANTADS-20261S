package br.dac.bantads.ms_conta.service;

import br.dac.bantads.ms_conta.dto.MovimentacaoRequestDTO;
import br.dac.bantads.ms_conta.model.ContaModel;
import br.dac.bantads.ms_conta.model.MovimentacaoModel;
import br.dac.bantads.ms_conta.model.enums.TipoMovimentacao;
import br.dac.bantads.ms_conta.repository.ContaRepository;
import br.dac.bantads.ms_conta.repository.MovimentacaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovimentacaoService {

    private final ContaRepository contaRepository;
    private final MovimentacaoRepository movimentacaoRepository;

    @Transactional
    public MovimentacaoModel realizarMovimentacao(UUID uuidContaOrigem, MovimentacaoRequestDTO request) {
        log.info("Iniciando movimentação do tipo {} para a conta {}. Valor: {}", request.tipo(), uuidContaOrigem, request.valor());

        if (request.valor() == null || request.valor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O valor da movimentação deve ser maior que zero");
        }

        ContaModel contaOrigem = contaRepository.findById(uuidContaOrigem)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta de origem não encontrada"));

        if (!contaOrigem.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A conta de origem está inativa ou pendente de aprovação");
        }

        MovimentacaoModel.MovimentacaoModelBuilder movimentacaoBuilder = MovimentacaoModel.builder()
                .conta(contaOrigem)
                .tipo(request.tipo())
                .valor(request.valor())
                .dataHora(LocalDateTime.now());

        switch (request.tipo()) {
            case DEPOSITO -> {
                contaOrigem.setSaldo(contaOrigem.getSaldo().add(request.valor()));
                contaRepository.save(contaOrigem);
            }
            case SAQUE -> {
                BigDecimal saldoDisponivel = contaOrigem.getSaldo().add(contaOrigem.getLimite());
                if (saldoDisponivel.compareTo(request.valor()) < 0) {
                    log.warn("Saldo insuficiente para saque na conta {}. Disponível: {}, Solicitado: {}", uuidContaOrigem, saldoDisponivel, request.valor());
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Saldo insuficiente (incluindo o limite)");
                }
                contaOrigem.setSaldo(contaOrigem.getSaldo().subtract(request.valor()));
                contaRepository.save(contaOrigem);
            }
            case TRANSFERENCIA -> {
                if (request.uuidContaDestino() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UUID da conta de destino é obrigatório para transferências");
                }
                if (uuidContaOrigem.equals(request.uuidContaDestino())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é possível realizar transferência para a própria conta");
                }

                ContaModel contaDestino = contaRepository.findById(request.uuidContaDestino())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta de destino não encontrada"));

                if (!contaDestino.isAtivo()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A conta de destino está inativa ou pendente de aprovação");
                }

                BigDecimal saldoDisponivel = contaOrigem.getSaldo().add(contaOrigem.getLimite());
                if (saldoDisponivel.compareTo(request.valor()) < 0) {
                    log.warn("Saldo insuficiente para transferência na conta {}. Disponível: {}, Solicitado: {}", uuidContaOrigem, saldoDisponivel, request.valor());
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Saldo insuficiente (incluindo o limite)");
                }

                // Debitar origem
                contaOrigem.setSaldo(contaOrigem.getSaldo().subtract(request.valor()));
                contaRepository.save(contaOrigem);

                // Creditar destino
                contaDestino.setSaldo(contaDestino.getSaldo().add(request.valor()));
                contaRepository.save(contaDestino);

                // Criar registro de movimentação de recebimento na conta destino
                MovimentacaoModel movimentacaoDestino = MovimentacaoModel.builder()
                        .conta(contaDestino)
                        .tipo(TipoMovimentacao.TRANSFERENCIA)
                        .valor(request.valor())
                        .uuidContaDestino(uuidContaOrigem) // Referência à conta de origem (quem enviou)
                        .dataHora(LocalDateTime.now())
                        .build();
                movimentacaoRepository.save(movimentacaoDestino);

                movimentacaoBuilder.uuidContaDestino(request.uuidContaDestino()); // Referência à conta de destino (quem recebeu)
            }
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo de movimentação desconhecido");
        }

        MovimentacaoModel movimentacaoOrigem = movimentacaoBuilder.build();
        return movimentacaoRepository.save(movimentacaoOrigem);
    }

    public List<MovimentacaoModel> obterExtrato(UUID uuidConta, LocalDateTime dataInicio, LocalDateTime dataFim) {
        log.info("Obtendo extrato da conta {} entre {} e {}", uuidConta, dataInicio, dataFim);
        if (!contaRepository.existsById(uuidConta)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta não encontrada");
        }
        return movimentacaoRepository.findByConta_UuidContaAndDataHoraBetweenOrderByDataHoraAsc(uuidConta, dataInicio, dataFim);
    }

    public List<MovimentacaoModel> obterMovimentacoes(UUID uuidConta) {
        log.info("Obtendo todas as movimentações da conta {}", uuidConta);
        ContaModel conta = contaRepository.findById(uuidConta)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta não encontrada"));
        return conta.getMovimentacoes();
    }
}
