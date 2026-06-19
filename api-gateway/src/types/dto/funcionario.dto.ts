// ─── DTOs do ms-funcionario ──────────────────────────────────────────

export interface FuncionarioDTO {
  id?: number;
  nome: string;
  cpf: string;
  email: string;
  telefone: string;
  tipo: string;
}

/** DTO composto usado na rota de API Composition GET /gerentes */
export interface GerenteComClientesDTO extends FuncionarioDTO {
  clientes: import('./conta.dto.js').ContaDTO[];
}
