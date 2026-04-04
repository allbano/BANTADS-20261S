import type { Movimentacao } from './movimentacao.model';

/**
 * Agrupamento diário do extrato (R8): movimentações do dia + saldo ao fim do dia.
 */
export interface ExtratoDia {
  data: string;
  movimentacoes: Movimentacao[];
  saldoConsolidadoFimDia: number;
}
