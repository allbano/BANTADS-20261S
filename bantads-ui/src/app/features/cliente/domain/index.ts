/**
 * Barrel file do domínio Cliente.
 *
 * Permite que outros bounded contexts (gerente, administrador)
 * importem os contratos do domínio através de
 * um único ponto de entrada:
 *   import { Cliente, ClienteRepository } from '@features/cliente/domain';
 */
export type { Cliente, EnderecoCliente } from './models/cliente.model';
export type { DashboardClienteResumo } from './models/dashboard-cliente-resumo.model';
export type { ExtratoDia } from './models/extrato-dia.model';
export type { GerenteResumo } from './models/gerente-resumo.model';
export type { Movimentacao } from './models/movimentacao.model';
export type { PerfilCliente, AlterarSenhaPayload } from './models/perfil-cliente.model';
export type { ResultadoOperacao } from './models/resultado-operacao.model';
export type { SentidoMovimentacao } from './models/sentido-movimentacao';
export type { TipoMovimentacao } from './models/tipo-movimentacao';
export { ClienteRepository } from './repositories/cliente.repository';
export { ClienteContaRepository } from './repositories/cliente-conta.repository';
export { DashboardClienteRepository } from './repositories/dashboard-cliente.repository';
export { PerfilClienteRepository } from './repositories/perfil-cliente.repository';
