export interface DadosPessoais {
  nome: string;
  cpf: string;
  telefone: string;
  email: string;
  salario: number;
}

export interface Endereco {
  cep: string;
  logradouro: string;
  numero: string;
  complemento?: string;
  cidade: string;
  uf: string;
}

export interface AutocadastroPayload {
  dadosPessoais: DadosPessoais;
  endereco: Endereco;
}
