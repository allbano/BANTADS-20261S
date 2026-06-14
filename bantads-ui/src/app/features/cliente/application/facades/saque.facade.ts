import { Injectable, inject, signal } from '@angular/core';

import type { DashboardClienteResumo } from '../../domain/models/dashboard-cliente-resumo.model';
import { ClienteContaRepository } from '../../domain/repositories/cliente-conta.repository';

@Injectable()
export class SaqueFacade {
  private readonly conta = inject(ClienteContaRepository);

  readonly resumo = signal<DashboardClienteResumo | null>(null);
  readonly feedback = signal<{ texto: string; erro: boolean } | null>(null);

  recarregar(): void {
    this.feedback.set(null);
    this.atualizarResumo();
  }

  sacar(valor: number): void {
    this.conta.sacar(valor).subscribe((r) => {
      this.feedback.set({ texto: r.mensagem, erro: !r.sucesso });
      if (r.sucesso) {
        this.atualizarResumo();
      }
    });
  }

  private atualizarResumo(): void {
    this.conta.obterResumo().subscribe({
      next: (r) => this.resumo.set(r),
      error: () => this.resumo.set(null),
    });
  }
}
