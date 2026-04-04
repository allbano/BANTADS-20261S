import type { GerenteDashboard } from '../models/gerente-dashboard.model';

/**
 * Contrato: obter estatísticas de gerentes para o dashboard do administrador (R15).
 */
export abstract class DashboardAdminRepository {
  /** Retorna as estatísticas de cada gerente (qtd clientes, saldos). */
  abstract obterEstatisticas(): GerenteDashboard[];
}
