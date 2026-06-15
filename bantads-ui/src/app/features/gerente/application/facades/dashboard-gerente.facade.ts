import { Injectable, inject, signal } from '@angular/core';

import type { PedidoAutocadastro } from '../../domain/models/pedido-autocadastro.model';
import { AprovacaoRepository } from '../../domain/repositories/aprovacao.repository';

/**
 * Facade da tela inicial do gerente (R9/R10/R11).
 * Gerencia o estado de pedidos pendentes e feedback de ações.
 */
@Injectable()
export class DashboardGerenteFacade {
  private readonly repository = inject(AprovacaoRepository);

  private readonly _pedidos = signal<PedidoAutocadastro[]>([]);
  private readonly _feedback = signal<{ texto: string; erro: boolean } | null>(null);

  readonly pedidos = this._pedidos.asReadonly();
  readonly feedback = this._feedback.asReadonly();

  carregar(): void {
    this._feedback.set(null);
    this.recarregarPendentes();
  }

  aprovar(cpf: string): void {
    this.repository.aprovar(cpf).subscribe((resultado) => {
      this._feedback.set({ texto: resultado.mensagem, erro: !resultado.sucesso });
      if (resultado.sucesso) {
        this.recarregarPendentes();
      }
    });
  }

  rejeitar(cpf: string, motivo: string): void {
    this.repository.rejeitar(cpf, motivo).subscribe((resultado) => {
      this._feedback.set({ texto: resultado.mensagem, erro: !resultado.sucesso });
      if (resultado.sucesso) {
        this.recarregarPendentes();
      }
    });
  }

  limparFeedback(): void {
    this._feedback.set(null);
  }

  private recarregarPendentes(): void {
    this.repository.listarPendentes().subscribe({
      next: (pedidos) => this._pedidos.set(pedidos),
      error: () => this._pedidos.set([]),
    });
  }
}
