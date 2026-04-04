import type { SentidoMovimentacao } from './sentido-movimentacao';
import type { TipoMovimentacao } from './tipo-movimentacao';

/**
 * Registro de movimentação na conta corrente do cliente.
 */
export interface Movimentacao {
  id: string;
  /** Instantâneo da transação (ISO 8601). */
  instante: string;
  tipo: TipoMovimentacao;
  sentido: SentidoMovimentacao;
  /** Valor absoluto; o sinal na UI vem do sentido. */
  valor: number;
  /** Origem/destino para transferências; vazio quando não aplicável. */
  contraparteLabel: string;
}
