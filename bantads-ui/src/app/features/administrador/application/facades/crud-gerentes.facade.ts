import { Injectable, computed, inject, signal } from '@angular/core';

import type { Gerente, GerenteAlteracao, GerenteInsercao } from '../../domain/models/gerente.model';
import { GerenteAdminRepository } from '../../domain/repositories/gerente-admin.repository';

/**
 * Facade para o CRUD de Gerentes do administrador (R17–R20, R19).
 *
 * A redistribuição de contas na inserção (R17) e remoção (R18) é tratada
 * pelo backend (SAGA); aqui apenas orquestramos as chamadas HTTP e o feedback.
 */
@Injectable()
export class CrudGerentesFacade {
  private readonly repository = inject(GerenteAdminRepository);

  private readonly _gerentes = signal<Gerente[]>([]);
  private readonly _feedback = signal<{ texto: string; erro: boolean } | null>(null);

  readonly feedback = this._feedback.asReadonly();

  /** R19: Gerentes ordenados crescente por nome. */
  readonly gerentesOrdenados = computed(() =>
    [...this._gerentes()].sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR')),
  );

  carregar(): void {
    this._feedback.set(null);
    this.recarregar();
  }

  /** R17: Inserção de gerente. */
  inserir(dados: GerenteInsercao): void {
    this.repository.inserir(dados).subscribe((resultado) => {
      this._feedback.set({ texto: resultado.mensagem, erro: !resultado.sucesso });
      if (resultado.sucesso) {
        this.recarregar();
      }
    });
  }

  /** R20: Alteração de gerente — nome, e-mail e senha. */
  atualizar(cpf: string, dados: GerenteAlteracao): void {
    this.repository.atualizar(cpf, dados).subscribe((resultado) => {
      this._feedback.set({ texto: resultado.mensagem, erro: !resultado.sucesso });
      if (resultado.sucesso) {
        this.recarregar();
      }
    });
  }

  /** R18: Remoção de gerente (o backend bloqueia a remoção do último). */
  remover(cpf: string): void {
    this.repository.remover(cpf).subscribe((resultado) => {
      this._feedback.set({ texto: resultado.mensagem, erro: !resultado.sucesso });
      if (resultado.sucesso) {
        this.recarregar();
      }
    });
  }

  limparFeedback(): void {
    this._feedback.set(null);
  }

  private recarregar(): void {
    this.repository.listarTodos().subscribe({
      next: (gerentes) => this._gerentes.set(gerentes),
      error: () => this._gerentes.set([]),
    });
  }
}
