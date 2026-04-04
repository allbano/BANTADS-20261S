/**
 * Barrel file do domínio Administrador.
 *
 * Permite que outros bounded contexts importem os contratos
 * do domínio através de um único ponto de entrada:
 *   import { Gerente, GerenteAdminRepository } from '@features/administrador/domain';
 */
export type { Gerente } from './models/gerente.model';
export type { Administrador } from './models/administrador.model';
export type { GerenteDashboard } from './models/gerente-dashboard.model';
export type { ClienteRelatorio } from './models/cliente-relatorio.model';
export { GerenteAdminRepository } from './repositories/gerente-admin.repository';
export { DashboardAdminRepository } from './repositories/dashboard-admin.repository';
export { RelatorioAdminRepository } from './repositories/relatorio-admin.repository';
