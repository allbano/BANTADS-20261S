import { Injectable } from '@angular/core';

import type { TopCliente } from '../../domain/models/top-cliente.model';
import { TopClientesRepository } from '../../domain/repositories/top-clientes.repository';
import { CLIENTES_GERENTE_MOCK } from '../data/clientes-gerente-mock';

/**
 * Implementação mock do TopClientesRepository (R14).
 *
 * Retorna os 3 clientes com maiores saldos de qualquer gerente,
 * ordenados de forma decrescente por saldo.
 */
@Injectable({ providedIn: 'root' })
export class TopClientesMockService extends TopClientesRepository {

  override obterTop3(): TopCliente[] {
    return [...CLIENTES_GERENTE_MOCK]
      .sort((a, b) => b.saldo - a.saldo)
      .slice(0, 3)
      .map(c => ({
        cpf: c.cpf,
        nome: c.nome,
        cidade: c.endereco.cidade,
        estado: c.endereco.uf,
        saldo: c.saldo,
      }));
  }
}
