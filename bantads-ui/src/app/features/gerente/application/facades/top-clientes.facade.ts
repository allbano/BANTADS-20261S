import { Injectable, inject, signal } from '@angular/core';

import type { TopCliente } from '../../domain/models/top-cliente.model';
import { TopClientesRepository } from '../../domain/repositories/top-clientes.repository';

/**
 * Facade para o ranking dos 3 maiores saldos (R14).
 */
@Injectable()
export class TopClientesFacade {
  private readonly repository = inject(TopClientesRepository);

  private readonly _topClientes = signal<TopCliente[]>([]);

  readonly topClientes = this._topClientes.asReadonly();

  carregar(): void {
    this._topClientes.set(this.repository.obterTop3());
  }
}
