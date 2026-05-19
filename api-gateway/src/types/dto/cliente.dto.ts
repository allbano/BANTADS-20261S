// ─── DTOs do ms-cliente ──────────────────────────────────────────────

export interface ClienteDTO {
  id?: number;
  nome: string;
  cpf: string;
  email: string;
  telefone: string;
  salario: number;
  endereco: EnderecoDTO;
}

export interface EnderecoDTO {
  cep: string;
  logradouro: string;
  numero: string;
  complemento?: string;
  bairro: string;
  cidade: string;
  estado: string;
}
