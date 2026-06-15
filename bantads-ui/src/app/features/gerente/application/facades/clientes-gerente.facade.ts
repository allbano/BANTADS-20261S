import { Injectable, computed, inject, signal } from '@angular/core';

import type { ClienteGerente } from '../../domain/models/cliente-gerente.model';
import { ClientesGerenteRepository } from '../../domain/repositories/clientes-gerente.repository';

/**
 * Facade para consulta de todos os clientes do gerente (R12) e detalhe (R13).
 *
 * Mantém a lista completa em memória e aplica filtro reativo por CPF/Nome.
 */
@Injectable()
export class ClientesGerenteFacade {
  private readonly repository = inject(ClientesGerenteRepository);

  private readonly _clientes = signal<ClienteGerente[]>([]);
  private readonly _filtro = signal<string>('');
  private readonly _selecionado = signal<ClienteGerente | null>(null);

  readonly filtro = this._filtro.asReadonly();
  readonly selecionado = this._selecionado.asReadonly();

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
    this.repository.listarTodos().subscribe({
      next: (clientes) => this._clientes.set(clientes),
      error: () => this._clientes.set([]),
    });
  }

  setFiltro(valor: string): void {
    this._filtro.set(valor);
  }

  carregarPorCpf(cpf: string): void {
    this.repository.buscarPorCpf(cpf).subscribe({
      next: (cliente) => this._selecionado.set(cliente),
      error: () => this._selecionado.set(null),
    });
  }
}
