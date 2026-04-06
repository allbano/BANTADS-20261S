import type { Cliente } from '../../domain/models/cliente.model';

/**
 * Clientes de demonstração (ambiente sem API).
 * Usados pelo login do cliente e pelo painel do gerente.
 */
export const CLIENTES_DEMONSTRACAO: Cliente[] = [
  {
    id: 1,
    nome: 'Catharyna',
    cpf: 12912861012,
    email: 'cli1@bantads.com.br',
    senha: 'tads',
    salario: 10000,
    telefone: '(41) 99100-1291',
    endereco: { cep: '80010-010', logradouro: 'Rua XV de Novembro', numero: '1291', complemento: 'Apto 12', cidade: 'Curitiba', uf: 'PR' },
  },
  {
    id: 2,
    nome: 'Cleuddônio',
    cpf: 19506382000,
    email: 'cli2@bantads.com.br',
    senha: 'tads',
    salario: 20000,
    telefone: '(41) 98800-0950',
    endereco: { cep: '80020-020', logradouro: 'Av. Sete de Setembro', numero: '950', cidade: 'Curitiba', uf: 'PR' },
  },
  {
    id: 3,
    nome: 'Catianna',
    cpf: 85733854057,
    email: 'cli3@bantads.com.br',
    senha: 'tads',
    salario: 3000,
    telefone: '(41) 97700-8573',
    endereco: { cep: '80030-030', logradouro: 'Rua das Flores', numero: '8573', cidade: 'Curitiba', uf: 'PR' },
  },
  {
    id: 4,
    nome: 'Cutardo',
    cpf: 58872160006,
    email: 'cli4@bantads.com.br',
    senha: 'tads',
    salario: 0,
    telefone: '(41) 96600-5887',
    endereco: { cep: '80040-040', logradouro: 'Rua do Porto', numero: '5887', cidade: 'Curitiba', uf: 'PR' },
  },
  {
    id: 5,
    nome: 'Coândrya',
    cpf: 76179646090,
    email: 'cli5@bantads.com.br',
    senha: 'tads',
    salario: 1500,
    telefone: '(41) 95500-7617',
    endereco: { cep: '80050-050', logradouro: 'Av. República Argentina', numero: '7617', cidade: 'Curitiba', uf: 'PR' },
  },
];
