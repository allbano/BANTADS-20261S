import type { EnderecoCliente } from './cliente.model';

/**
 * Payload de edição do perfil do cliente.
 *
 * Separado de Cliente para respeitar SRP — esta interface carrega
 * apenas os campos que o próprio usuário pode visualizar e alterar
 * na tela "Meu Perfil".
 */
export interface PerfilCliente {
  /** CPF é readonly — exibido mas nunca alterado pelo cliente. */
  cpf: string;
  nome: string;
  email: string;
  /** Salário do cliente, pode ser atualizado pelo próprio cliente. */
  salario: number;
  telefone: string;
  endereco: EnderecoCliente;
}
