import { Injectable, computed, inject, signal } from '@angular/core';

import type { ClienteGerente } from '../../domain/models/cliente-gerente.model';
import { ClientesGerenteRepository } from '../../domain/repositories/clientes-gerente.repository';

/**
 * Facade para consulta de todos os clientes do gerente (R12).
 *
 * Mantém a lista completa em memória e aplica filtro reativo por CPF/Nome.
 */
@Injectable()
export class ClientesGerenteFacade {
  private readonly repository = inject(ClientesGerenteRepository);

  private readonly _clientes = signal<ClienteGerente[]>([]);
  private readonly _filtro = signal<string>('');

  readonly filtro = this._filtro.asReadonly();

  /** Lista filtrada por CPF ou nome (parcial), ordenada por nome ASC. */
  readonly clientesFiltrados = computed(() => {
    const termo = this._filtro().trim().toLowerCase();
    let lista = this._clientes();

    if (termo) {
      lista = lista.filter(c =>
        c.nome.toLowerCase().includes(termo) ||
        c.cpf.includes(termo)
      );
    }

    return [...lista].sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'));
  });

  carregar(): void {
    this._clientes.set(this.repository.listarTodos());
  }

  setFiltro(valor: string): void {
    this._filtro.set(valor);
  }

  buscarPorId(id: number): ClienteGerente | null {
    return this.repository.buscarPorId(id);
  }
}
