/**
 * Entidade de domínio que representa o Administrador no BANTADS.
 */
export interface Administrador {
  id: number;
  nome: string;
  cpf: string;
  email: string;
  senha: string;
  tipo: 'administrador';
}
