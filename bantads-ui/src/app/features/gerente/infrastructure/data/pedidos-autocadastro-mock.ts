import type { PedidoAutocadastro } from '../../domain/models/pedido-autocadastro.model';

/**
 * Pedidos de autocadastro pendentes (ambiente sem API).
 * Usados pela tela inicial do gerente (R9).
 */
export const PEDIDOS_AUTOCADASTRO_MOCK: PedidoAutocadastro[] = [
  {
    id: 101,
    nome: 'Albânia Ferreira',
    cpf: '32165498701',
    email: 'albania@email.com',
    telefone: '(41) 99876-5432',
    salario: 4500,
    endereco: {
      cep: '80010-000',
      logradouro: 'Rua das Flores',
      numero: '123',
      complemento: 'Apto 4B',
      cidade: 'Curitiba',
      uf: 'PR',
    },
    dataSolicitacao: '2026-04-01T14:30:00',
  },
  {
    id: 102,
    nome: 'Bráulio Mendonça',
    cpf: '98765432100',
    email: 'braulio@email.com',
    telefone: '(11) 91234-5678',
    salario: 1800,
    endereco: {
      cep: '01001-000',
      logradouro: 'Praça da Sé',
      numero: '456',
      cidade: 'São Paulo',
      uf: 'SP',
    },
    dataSolicitacao: '2026-04-02T09:15:00',
  },
  {
    id: 103,
    nome: 'Dulcinéia Rocha',
    cpf: '45678912300',
    email: 'dulcineia@email.com',
    telefone: '(21) 98765-1234',
    salario: 7200,
    endereco: {
      cep: '20040-020',
      logradouro: 'Av. Rio Branco',
      numero: '789',
      complemento: 'Sala 302',
      cidade: 'Rio de Janeiro',
      uf: 'RJ',
    },
    dataSolicitacao: '2026-04-03T16:45:00',
  },
  {
    id: 104,
    nome: 'Epaminondas Silva',
    cpf: '11122233344',
    email: 'epaminondas@email.com',
    telefone: '(31) 97654-3210',
    salario: 2000,
    endereco: {
      cep: '30130-000',
      logradouro: 'Rua da Bahia',
      numero: '1010',
      cidade: 'Belo Horizonte',
      uf: 'MG',
    },
    dataSolicitacao: '2026-04-03T18:00:00',
  },
];
