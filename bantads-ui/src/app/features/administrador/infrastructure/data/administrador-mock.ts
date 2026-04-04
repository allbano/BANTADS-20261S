import type { Administrador } from '../../domain/models/administrador.model';

/**
 * Mock do Administrador (ambiente sem API).
 *
 * Dados extraídos da tabela "Gerente/Administrador e Auth" fornecida:
 * - Adamântio (CPF 40501740066, adm1@bantads.com.br)
 */
export const ADMINISTRADOR_MOCK: Administrador = {
  id: 1,
  nome: 'Adamântio',
  cpf: '40501740066',
  email: 'adm1@bantads.com.br',
  senha: 'tads',
  tipo: 'administrador',
};
