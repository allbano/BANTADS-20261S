import type { GerenteResumo } from './gerente-resumo.model';
import type { Movimentacao } from './movimentacao.model';

/**
 * Agregado de leitura para a tela inicial do cliente (R3) e trechos do extrato (R8).
 */
export interface DashboardClienteResumo {
  clienteId: number;
  nomeCliente: string;
  emailCliente: string;
  numeroConta: string;
  saldo: number;
  salario: number;
  limiteCredito: number;
  gerente: GerenteResumo;
  /** Data de abertura da conta (ISO date yyyy-mm-dd). */
  dataAberturaConta: string;
  ultimasMovimentacoes: Movimentacao[];
}
