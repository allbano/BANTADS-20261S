// ─── DTOs do ms-auth ────────────────────────────────────────────────

export interface LoginRequestDTO {
  login: string;
  senha: string;
}

export interface LoginResponseDTO {
  token: string;
  tipo: 'CLIENTE' | 'GERENTE' | 'ADMIN';
}
