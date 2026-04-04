import { Injectable, inject, signal } from '@angular/core';

import type { PedidoAutocadastro } from '../../domain/models/pedido-autocadastro.model';
import { AprovacaoRepository } from '../../domain/repositories/aprovacao.repository';

/**
 * Facade da tela inicial do gerente (R9/R10/R11).
 *
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
    this._pedidos.set(this.repository.listarPendentes());
    this._feedback.set(null);
  }

  aprovar(pedidoId: number): void {
    const resultado = this.repository.aprovar(pedidoId);
    this._feedback.set({ texto: resultado.mensagem, erro: !resultado.sucesso });
    if (resultado.sucesso) {
      this._pedidos.set(this.repository.listarPendentes());
    }
  }

  rejeitar(pedidoId: number, motivo: string): void {
    const resultado = this.repository.rejeitar(pedidoId, motivo);
    this._feedback.set({ texto: resultado.mensagem, erro: !resultado.sucesso });
    if (resultado.sucesso) {
      this._pedidos.set(this.repository.listarPendentes());
    }
  }

  limparFeedback(): void {
    this._feedback.set(null);
  }
}
