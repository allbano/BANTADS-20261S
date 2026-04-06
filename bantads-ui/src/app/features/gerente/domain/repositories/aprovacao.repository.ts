import type { PedidoAutocadastro } from '../models/pedido-autocadastro.model';
import type { ResultadoAprovacao } from '../models/resultado-aprovacao.model';

/**
 * Contrato do repositório de aprovação/rejeição de autocadastros (R9/R10/R11).
 *
 * A camada de domínio define o "o quê"; a infraestrutura define o "como".
 */
export abstract class AprovacaoRepository {
  /** Lista todos os pedidos pendentes de decisão (opcionalmente filtrados por gerente). */
  abstract listarPendentes(gerenteId?: number): PedidoAutocadastro[];

  /** Registra um novo pedido com gerente já designado no momento do autocadastro (R1). */
  abstract registrarPedido(pedido: PedidoAutocadastro): void;

  /**
   * Aprova um pedido: cria conta (número 4 dígitos), calcula limite,
   * gera senha e "envia" e-mail (R10).
   */
  abstract aprovar(pedidoId: number): ResultadoAprovacao;

  /**
   * Rejeita um pedido: registra motivo e data/hora,
   * "envia" e-mail com motivo da reprovação (R11).
   */
  abstract rejeitar(pedidoId: number, motivo: string): ResultadoAprovacao;
}
