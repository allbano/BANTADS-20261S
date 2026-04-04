import type { DashboardClienteResumo } from '../models/dashboard-cliente-resumo.model';

/**
 * Contrato: obter o resumo da área logada do cliente (conta + últimas movimentações).
 */
export abstract class DashboardClienteRepository {
  abstract obterResumo(clienteId: number): DashboardClienteResumo | null;
}
