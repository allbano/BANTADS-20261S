import type { Endereco } from '../../../cliente/domain/models/autocadastro.model';

/**
 * Pedido de autocadastro pendente de aprovação pelo gerente (R9).
 *
 * Contém os dados pessoais e endereço que o cliente enviou
 * durante o autocadastro, aguardando decisão.
 */
export interface PedidoAutocadastro {
  id: number;
  nome: string;
  cpf: string;
  email: string;
  telefone: string;
  salario: number;
  endereco: Endereco;
  /** ISO 8601 — data/hora da solicitação. */
  dataSolicitacao: string;
}
