/**
 * Entidade de domínio que representa um Gerente no BANTADS.
 *
 * Em DDD, modelos de domínio são contratos imutáveis — usamos interface
 * ao invés de class para evitar acoplamento com instanciação direta.
 */
export interface Gerente {
  id: number;
  nome: string;
  cpf: string;
  email: string;
  telefone: string;
  senha: string;
  tipo: 'gerente';
}
