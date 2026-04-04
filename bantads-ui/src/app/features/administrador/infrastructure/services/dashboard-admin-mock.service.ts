import { Injectable, inject } from '@angular/core';

import type { GerenteDashboard } from '../../domain/models/gerente-dashboard.model';
import { DashboardAdminRepository } from '../../domain/repositories/dashboard-admin.repository';
import { ClienteContaMockService } from '../../../cliente/infrastructure/services/cliente-conta-mock.service';
import { CLIENTES_DEMONSTRACAO } from '../../../cliente/infrastructure/data/clientes-demonstracao';
import { GERENTES_MOCK } from '../data/gerentes-mock';

/**
 * Mapeamento fixo cliente → gerente (extraído das imagens "Conta").
 *
 * | Cliente      | Gerente     |
 * |-------------|-------------|
 * | Catharyna   | Geniéve     |
 * | Cleuddônio  | Godophredo  |
 * | Catianna    | Gyândula    |
 * | Cutardo     | Geniéve     |
 * | Coândrya    | Godophredo  |
 */
const MAPA_CLIENTE_GERENTE: Record<number, number> = {
  1: 1, // Catharyna  → Geniéve
  2: 2, // Cleuddônio → Godophredo
  3: 3, // Catianna   → Gyândula
  4: 1, // Cutardo    → Geniéve
  5: 2, // Coândrya   → Godophredo
};

/**
 * Implementação mock do DashboardAdminRepository (R15).
 *
 * Cruza dados de gerentes com contas de clientes para calcular
 * as estatísticas por gerente. Dados calculados dinamicamente
 * a partir do ClienteContaMockService.
 */
@Injectable({ providedIn: 'root' })
export class DashboardAdminMockService extends DashboardAdminRepository {
  private readonly contaService = inject(ClienteContaMockService);

  override obterEstatisticas(): GerenteDashboard[] {
    const stats = new Map<number, { nome: string; qtd: number; pos: number; neg: number }>();

    // Inicializa todos os gerentes
    for (const g of GERENTES_MOCK) {
      stats.set(g.id, { nome: g.nome, qtd: 0, pos: 0, neg: 0 });
    }

    // Itera sobre cada cliente → obtém resumo da conta → acumula no gerente
    for (const cliente of CLIENTES_DEMONSTRACAO) {
      const gerenteId = MAPA_CLIENTE_GERENTE[cliente.id];
      if (!gerenteId) continue;

      const resumo = this.contaService.obterResumo(cliente.id);
      if (!resumo) continue;

      const entry = stats.get(gerenteId);
      if (!entry) continue;

      entry.qtd += 1;
      if (resumo.saldo >= 0) {
        entry.pos += resumo.saldo;
      } else {
        entry.neg += resumo.saldo;
      }
    }

    const resultado: GerenteDashboard[] = [];
    for (const [gerenteId, entry] of stats.entries()) {
      resultado.push({
        gerenteId,
        nome: entry.nome,
        qtdClientes: entry.qtd,
        saldoPositivo: entry.pos,
        saldoNegativo: entry.neg,
      });
    }

    return resultado;
  }
}
