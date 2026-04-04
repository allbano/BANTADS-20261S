/**
 * Barrel file do domínio Gerente.
 *
 * Permite que outros bounded contexts importem os contratos
 * do domínio através de um único ponto de entrada:
 *   import { ClienteGerente, AprovacaoRepository } from '@features/gerente/domain';
 */
export type { PedidoAutocadastro } from './models/pedido-autocadastro.model';
export type { ClienteGerente } from './models/cliente-gerente.model';
export type { ResultadoAprovacao } from './models/resultado-aprovacao.model';
export type { TopCliente } from './models/top-cliente.model';
export { AprovacaoRepository } from './repositories/aprovacao.repository';
export { ClientesGerenteRepository } from './repositories/clientes-gerente.repository';
export { TopClientesRepository } from './repositories/top-clientes.repository';
