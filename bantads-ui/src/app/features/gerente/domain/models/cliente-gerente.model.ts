import type { Endereco } from '../../../cliente/domain/models/autocadastro.model';

/**
 * Visão completa de um cliente aprovado, conforme necessário
 * para o gerente (R12 — tabela e R13 — consulta por CPF).
 */
export interface ClienteGerente {
  id: number;
  nome: string;
  cpf: string;
  email: string;
  telefone: string;
  salario: number;
  endereco: Endereco;

  /** Dados da conta corrente (criada após aprovação). */
  numeroConta: string;
  saldo: number;
  limite: number;
  /** ISO 8601 — data de abertura da conta. */
  dataAberturaConta: string;
}
