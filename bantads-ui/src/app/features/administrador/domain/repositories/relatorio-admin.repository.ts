import type { ClienteRelatorio } from '../models/cliente-relatorio.model';

/**
 * Contrato: obter lista completa de clientes para o relatório do administrador (R16).
 */
export abstract class RelatorioAdminRepository {
  /** Retorna todos os clientes com dados completos (conta + gerente). */
  abstract listarClientes(): ClienteRelatorio[];
}
