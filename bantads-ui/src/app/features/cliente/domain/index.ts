/**
 * Barrel file do domínio Cliente.
 *
 * Permite que outros bounded contexts (gerente, administrador)
 * importem os contratos do domínio via um único ponto de entrada:
 *
 *   import { Cliente, ClienteRepository } from '@features/cliente/domain';
 */
export type { Cliente } from './models/cliente.model';
export { ClienteRepository } from './repositories/cliente.repository';
