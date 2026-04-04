import { Injectable } from '@angular/core';

import type { ClienteGerente } from '../../domain/models/cliente-gerente.model';
import { ClientesGerenteRepository } from '../../domain/repositories/clientes-gerente.repository';
import { CLIENTES_GERENTE_MOCK } from '../data/clientes-gerente-mock';

/**
 * Implementação mock do ClientesGerenteRepository (R12/R13).
 *
 * Busca clientes no array em memória. Quando a API estiver pronta,
 * basta criar um novo service que implemente o mesmo contrato via HTTP.
 */
@Injectable({ providedIn: 'root' })
export class ClientesGerenteMockService extends ClientesGerenteRepository {

  override listarTodos(): ClienteGerente[] {
    return [...CLIENTES_GERENTE_MOCK];
  }

  override buscarPorCpf(cpf: string): ClienteGerente | null {
    const normalizado = cpf.replace(/\D/g, '');
    return CLIENTES_GERENTE_MOCK.find(c => c.cpf === normalizado) ?? null;
  }

  override buscarPorId(id: number): ClienteGerente | null {
    return CLIENTES_GERENTE_MOCK.find(c => c.id === id) ?? null;
  }
}
