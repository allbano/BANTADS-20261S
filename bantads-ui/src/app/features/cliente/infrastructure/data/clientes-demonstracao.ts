import type { Cliente } from '../../domain/models/cliente.model';

/**
 * Clientes de demonstração (ambiente sem API).
 * Usados pelo login do cliente e pelo painel do gerente.
 */
export const CLIENTES_DEMONSTRACAO: Cliente[] = [
  { id: 1, nome: 'Catharyna', cpf: 12912861012, email: 'cli1@bantads.com.br', senha: 'tads', salario: 10000 },
  { id: 2, nome: 'Cleuddônio', cpf: 19506382000, email: 'cli2@bantads.com.br', senha: 'tads', salario: 20000 },
  { id: 3, nome: 'Catianna', cpf: 85733854057, email: 'cli3@bantads.com.br', senha: 'tads', salario: 3000 },
  { id: 4, nome: 'Cutardo', cpf: 58872160006, email: 'cli4@bantads.com.br', senha: 'tads', salario: 0 },
  { id: 5, nome: 'Coândrya', cpf: 76179646090, email: 'cli5@bantads.com.br', senha: 'tads', salario: 1500 },
];
