import type { Endereco } from '../../../cliente/domain/models/autocadastro.model';

/**
 * Pedido de autocadastro pendente de aprovação pelo gerente (R9).
 *
 * Vem de GET /clientes?filtro=para_aprovar. A identidade é o CPF.
 */
export interface PedidoAutocadastro {
  cpf: string;
  nome: string;
  email: string;
  telefone: string;
  salario: number;
  endereco: Endereco;
  /** ISO 8601 — data/hora da solicitação, quando disponível. */
  dataSolicitacao?: string;
}
