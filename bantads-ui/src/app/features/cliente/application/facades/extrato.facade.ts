import { Injectable, inject, signal } from '@angular/core';

import type { DashboardClienteResumo } from '../../domain/models/dashboard-cliente-resumo.model';
import type { ExtratoDia } from '../../domain/models/extrato-dia.model';
import { ClienteContaRepository } from '../../domain/repositories/cliente-conta.repository';

@Injectable()
export class ExtratoFacade {
  private readonly conta = inject(ClienteContaRepository);

  readonly resumo = signal<DashboardClienteResumo | null>(null);
  readonly dias = signal<ExtratoDia[]>([]);

  /** Atualiza apenas o resumo (saldo atual no cabeçalho). */
  atualizarResumo(): void {
    this.conta.obterResumo().subscribe({
      next: (r) => this.resumo.set(r),
      error: () => this.resumo.set(null),
    });
  }

  aplicarFiltro(dataInicio: string, dataFim: string): void {
    this.conta.consultarExtrato(dataInicio, dataFim).subscribe({
      next: (dias) => this.dias.set(dias),
      error: () => this.dias.set([]),
    });
  }
}
