/**
 * Entidade de domínio que representa um Gerente no BANTADS (visão do administrador).
 * A identidade é o CPF (string).
 */
export interface Gerente {
  cpf: string;
  nome: string;
  email: string;
  telefone?: string;
  /** "GERENTE" ou "ADMINISTRADOR", conforme a API Gateway. */
  tipo: string;
}

/** Payload de inserção de gerente (R17 — POST /gerentes). */
export interface GerenteInsercao {
  cpf: string;
  nome: string;
  email: string;
  telefone: string;
  tipo: string;
  senha: string;
}

/** Payload de alteração de gerente (R20 — PUT /gerentes/{cpf}). */
export interface GerenteAlteracao {
  nome: string;
  email: string;
  senha?: string;
}
