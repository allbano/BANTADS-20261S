import type { Observable } from 'rxjs';
import type { DashboardClienteResumo } from '../models/dashboard-cliente-resumo.model';

/**
 * Contrato: obter o resumo da área logada do cliente (conta + últimas movimentações).
 * A identidade (CPF/número de conta) vem da sessão dentro da implementação.
 */
export abstract class DashboardClienteRepository {
  abstract obterResumo(): Observable<DashboardClienteResumo>;
}
