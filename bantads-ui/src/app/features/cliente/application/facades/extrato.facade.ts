import { Injectable, inject, signal } from '@angular/core';

import type { DashboardClienteResumo } from '../../domain/models/dashboard-cliente-resumo.model';
import type { ExtratoDia } from '../../domain/models/extrato-dia.model';
import { ClienteContaMockService } from '../../infrastructure/services/cliente-conta-mock.service';
import { SessaoClienteService } from '../../../../core/auth/services/sessao-cliente.service';

@Injectable()
export class ExtratoFacade {
  private readonly conta = inject(ClienteContaMockService);
  private readonly sessao = inject(SessaoClienteService);

  readonly resumo = signal<DashboardClienteResumo | null>(null);
  readonly dias = signal<ExtratoDia[]>([]);

  /** Atualiza apenas o resumo (saldo atual no cabeçalho). */
  atualizarResumo(): void {
    const id = this.sessao.clienteId();
    if (id === null) {
      this.resumo.set(null);
      return;
    }
    this.resumo.set(this.conta.obterResumo(id));
  }

  /** Resumo + período padrão Catharyna (jan/2020). */
  recarregar(): void {
    this.atualizarResumo();
    this.aplicarFiltro('2020-01-01', '2020-01-31');
  }

  aplicarFiltro(dataInicio: string, dataFim: string): void {
    const id = this.sessao.clienteId();
    if (id === null) {
      this.dias.set([]);
      return;
    }
    this.dias.set(this.conta.consultarExtrato(id, dataInicio, dataFim));
  }
}
