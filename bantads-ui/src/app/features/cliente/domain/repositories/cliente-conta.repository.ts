import type { ExtratoDia } from '../models/extrato-dia.model';
import type { ResultadoOperacao } from '../models/resultado-operacao.model';
import { DashboardClienteRepository } from './dashboard-cliente.repository';

/**
 * Operações de conta corrente do cliente (R5–R8) + leitura do dashboard (R3).
 */
export abstract class ClienteContaRepository extends DashboardClienteRepository {
  abstract depositar(clienteId: number, valor: number): ResultadoOperacao;
  abstract sacar(clienteId: number, valor: number): ResultadoOperacao;
  abstract transferir(clienteIdOrigem: number, numeroContaDestino: string, valor: number): ResultadoOperacao;
  abstract consultarExtrato(clienteId: number, dataInicioIso: string, dataFimIso: string): ExtratoDia[];
  abstract atualizarLimiteCredito(clienteId: number, novoLimite: number): ResultadoOperacao;
}
