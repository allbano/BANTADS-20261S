import { Injectable, inject, signal } from '@angular/core';

import type { ClienteGerente } from '../../domain/models/cliente-gerente.model';
import { ClientesGerenteRepository } from '../../domain/repositories/clientes-gerente.repository';

/**
 * Facade para consulta de cliente por CPF (R13).
 */
@Injectable()
export class ConsultaClienteFacade {
  private readonly repository = inject(ClientesGerenteRepository);

  private readonly _cliente = signal<ClienteGerente | null>(null);
  private readonly _erro = signal<string | null>(null);
  private readonly _pesquisou = signal<boolean>(false);

  readonly cliente = this._cliente.asReadonly();
  readonly erro = this._erro.asReadonly();
  readonly pesquisou = this._pesquisou.asReadonly();

  pesquisarPorCpf(cpf: string): void {
    this._pesquisou.set(true);
    const normalizado = cpf.replace(/\D/g, '');

    if (!normalizado) {
      this._erro.set('Informe um CPF para pesquisar.');
      this._cliente.set(null);
      return;
    }

    const encontrado = this.repository.buscarPorCpf(normalizado);
    if (encontrado) {
      this._cliente.set(encontrado);
      this._erro.set(null);
    } else {
      this._cliente.set(null);
      this._erro.set('Nenhum cliente encontrado com o CPF informado.');
    }
  }

  limpar(): void {
    this._cliente.set(null);
    this._erro.set(null);
    this._pesquisou.set(false);
  }
}
