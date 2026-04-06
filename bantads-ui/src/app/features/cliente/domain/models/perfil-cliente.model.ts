import type { EnderecoCliente } from './cliente.model';

/**
 * Payload de edição do perfil do cliente.
 *
 * Separado de Cliente para respeitar SRP — esta interface carrega
 * apenas os campos que o próprio usuário pode visualizar e alterar
 * na tela "Meu Perfil".  Campos como `senha` ficam em fluxo próprio.
 */
export interface PerfilCliente {
  clienteId: number;
  nome: string;
  /** CPF é readonly — exibido mas nunca alterado pelo cliente. */
  cpf: number;
  email: string;
  /** Salário do cliente, pode ser atualizado pelo próprio cliente. */
  salario: number;
  telefone: string;
  endereco: EnderecoCliente;
}

/**
 * Payload para alteração de senha (fluxo separado, dentro da mesma tela).
 */
export interface AlterarSenhaPayload {
  clienteId: number;
  senhaAtual: string;
  novaSenha: string;
}
