import { Injectable, computed, inject, signal } from '@angular/core';

import type { GerenteDashboard } from '../../domain/models/gerente-dashboard.model';
import { DashboardAdminRepository } from '../../domain/repositories/dashboard-admin.repository';

/**
 * Facade para o dashboard do administrador (R15).
 *
 * Carrega as estatísticas de gerentes e expõe computed
 * ordenada por saldo positivo descrescente.
 */
@Injectable()
export class DashboardAdminFacade {
  private readonly repository = inject(DashboardAdminRepository);

  private readonly _estatisticas = signal<GerenteDashboard[]>([]);
  private readonly _erro = signal<string | null>(null);

  readonly erro = this._erro.asReadonly();

  /** R15: Gerentes ordenados por maior saldo positivo primeiro. */
  readonly estatisticasOrdenadas = computed(() => {
    return [...this._estatisticas()].sort((a, b) => b.saldoPositivo - a.saldoPositivo);
  });

  carregar(): void {
    this.repository.obterEstatisticas().subscribe({
      next: (dados) => {
        this._estatisticas.set(dados);
        this._erro.set(null);
      },
      error: () => {
        this._erro.set('Não foi possível carregar as estatísticas.');
        this._estatisticas.set([]);
      },
    });
  }
}
