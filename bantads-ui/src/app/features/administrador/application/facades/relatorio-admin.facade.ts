import { Injectable, computed, inject, signal } from '@angular/core';

import type { ClienteRelatorio } from '../../domain/models/cliente-relatorio.model';
import { RelatorioAdminRepository } from '../../domain/repositories/relatorio-admin.repository';

/**
 * Facade para o relatório de clientes do administrador (R16).
 *
 * Mantém a lista completa em memória e aplica filtro reativo
 * por CPF, Nome ou E-mail. Ordenação crescente por nome.
 */
@Injectable()
export class RelatorioAdminFacade {
  private readonly repository = inject(RelatorioAdminRepository);

  private readonly _clientes = signal<ClienteRelatorio[]>([]);
  private readonly _filtro = signal<string>('');

  readonly filtro = this._filtro.asReadonly();

  /** R16: Lista filtrada e ordenada crescente por nome do cliente. */
  readonly clientesFiltrados = computed(() => {
    const termo = this._filtro().trim().toLowerCase();
    let lista = this._clientes();

    if (termo) {
      lista = lista.filter(c =>
        c.nome.toLowerCase().includes(termo) ||
        c.cpf.includes(termo) ||
        c.email.toLowerCase().includes(termo)
      );
    }

    return [...lista].sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'));
  });

  carregar(): void {
    this._clientes.set(this.repository.listarClientes());
  }

  setFiltro(valor: string): void {
    this._filtro.set(valor);
  }
}
