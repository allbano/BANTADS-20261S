import type { Gerente } from '../../domain/models/gerente.model';

/**
 * Gerentes de demonstração (ambiente sem API).
 *
 * Dados extraídos da tabela "Gerente/Administrador e Auth" fornecida:
 * - Geniéve  (CPF 98574307084, ger1@bantads.com.br)
 * - Godophredo (CPF 64065268052, ger2@bantads.com.br)
 * - Gyândula (CPF 23862179060, ger3@bantads.com.br)
 */
export const GERENTES_MOCK: Gerente[] = [
  {
    id: 1,
    nome: 'Geniéve',
    cpf: '98574307084',
    email: 'ger1@bantads.com.br',
    telefone: '(41) 99111-0001',
    senha: 'tads',
    tipo: 'gerente',
  },
  {
    id: 2,
    nome: 'Godophredo',
    cpf: '64065268052',
    email: 'ger2@bantads.com.br',
    telefone: '(41) 99222-0002',
    senha: 'tads',
    tipo: 'gerente',
  },
  {
    id: 3,
    nome: 'Gyândula',
    cpf: '23862179060',
    email: 'ger3@bantads.com.br',
    telefone: '(41) 99333-0003',
    senha: 'tads',
    tipo: 'gerente',
  },
];
