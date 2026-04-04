import { Injectable, computed, inject, signal } from '@angular/core';

import type { DashboardClienteResumo } from '../../domain/models/dashboard-cliente-resumo.model';
import { DashboardClienteRepository } from '../../domain/repositories/dashboard-cliente.repository';
import { SessaoClienteService } from '../../../../core/auth/services/sessao-cliente.service';

@Injectable()
export class DashboardClienteFacade {
  private readonly repository = inject(DashboardClienteRepository);
  private readonly sessao = inject(SessaoClienteService);

  private readonly _resumo = signal<DashboardClienteResumo | null>(null);
  private readonly _erro = signal<string | null>(null);

  readonly resumo = this._resumo.asReadonly();
  readonly erro = this._erro.asReadonly();

  /** Capacidade de movimentação: saldo + limite (R6). */
  readonly capacidadeTotal = computed(() => {
    const r = this._resumo();
    if (!r) {
      return 0;
    }
    return r.saldo + r.limiteCredito;
  });

  readonly saldoEhNegativo = computed(() => {
    const r = this._resumo();
    return r !== null && r.saldo < 0;
  });

  carregar(): void {
    const id = this.sessao.clienteId();
    if (id === null) {
      this._erro.set('Sessão inválida.');
      this._resumo.set(null);
      return;
    }
    const dados = this.repository.obterResumo(id);
    if (!dados) {
      this._erro.set('Não foi possível carregar os dados da conta.');
      this._resumo.set(null);
      return;
    }
    this._erro.set(null);
    this._resumo.set(dados);
  }
}
