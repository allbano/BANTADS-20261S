import type { Observable } from 'rxjs';

import type { ExtratoDia } from '../models/extrato-dia.model';
import type { ResultadoOperacao } from '../models/resultado-operacao.model';
import { DashboardClienteRepository } from './dashboard-cliente.repository';

/**
 * Operações de conta corrente do cliente (R5–R8) + leitura do dashboard (R3).
 * O número da conta vem da sessão dentro da implementação.
 */
export abstract class ClienteContaRepository extends DashboardClienteRepository {
  abstract depositar(valor: number): Observable<ResultadoOperacao>;
  abstract sacar(valor: number): Observable<ResultadoOperacao>;
  abstract transferir(numeroContaDestino: string, valor: number): Observable<ResultadoOperacao>;
  abstract consultarExtrato(dataInicioIso: string, dataFimIso: string): Observable<ExtratoDia[]>;
}
