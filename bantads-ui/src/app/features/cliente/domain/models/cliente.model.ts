/**
 * Endereço completo de um cliente.
 */
export interface EnderecoCliente {
  cep: string;
  logradouro: string;
  numero: string;
  complemento?: string;
  cidade: string;
  uf: string;
}

/**
 * Entidade de domínio que representa um Cliente no BANTADS.
 *
 * Em DDD, modelos de domínio são contratos imutáveis — usamos interface
 * ao invés de class para evitar acoplamento com instanciação direta.
 */
export interface Cliente {
  id: number;
  nome: string;
  cpf: number;
  email: string;
  senha: string;
  salario: number;
  telefone?: string;
  endereco?: EnderecoCliente;
}
