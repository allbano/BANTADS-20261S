import type { Observable } from 'rxjs';

import type { ClienteGerente } from '../models/cliente-gerente.model';

/**
 * Contrato do repositório de clientes do gerente (R12/R13).
 */
export abstract class ClientesGerenteRepository {
  /** Retorna todos os clientes com conta (R12). */
  abstract listarTodos(): Observable<ClienteGerente[]>;

  /** Busca um cliente pelo CPF exato (R13). */
  abstract buscarPorCpf(cpf: string): Observable<ClienteGerente | null>;
}
