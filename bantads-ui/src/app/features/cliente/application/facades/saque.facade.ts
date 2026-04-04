import { Injectable, inject, signal } from '@angular/core';

import type { DashboardClienteResumo } from '../../domain/models/dashboard-cliente-resumo.model';
import { ClienteContaMockService } from '../../infrastructure/services/cliente-conta-mock.service';
import { SessaoClienteService } from '../../../../core/auth/services/sessao-cliente.service';

@Injectable()
export class SaqueFacade {
  private readonly conta = inject(ClienteContaMockService);
  private readonly sessao = inject(SessaoClienteService);

  readonly resumo = signal<DashboardClienteResumo | null>(null);
  readonly feedback = signal<{ texto: string; erro: boolean } | null>(null);

  recarregar(): void {
    const id = this.sessao.clienteId();
    this.feedback.set(null);
    if (id === null) {
      this.resumo.set(null);
      return;
    }
    this.resumo.set(this.conta.obterResumo(id));
  }

  sacar(valor: number): void {
    const id = this.sessao.clienteId();
    if (id === null) {
      return;
    }
    const r = this.conta.sacar(id, valor);
    if (r.sucesso) {
      this.feedback.set({ texto: r.mensagem, erro: false });
      this.recarregar();
    } else {
      this.feedback.set({ texto: r.mensagem, erro: true });
    }
  }
}
