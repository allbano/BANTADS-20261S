import type { ClienteGerente } from '../models/cliente-gerente.model';

/**
 * Contrato do repositório de clientes do gerente (R12/R13).
 */
export abstract class ClientesGerenteRepository {
  /** Retorna todos os clientes do gerente, sem filtro. */
  abstract listarTodos(): ClienteGerente[];

  /** Busca um cliente pelo CPF exato. */
  abstract buscarPorCpf(cpf: string): ClienteGerente | null;

  /** Busca um cliente pelo ID. */
  abstract buscarPorId(id: number): ClienteGerente | null;
}
