import type { TopCliente } from '../models/top-cliente.model';

/**
 * Contrato para obter os 3 maiores saldos do sistema (R14).
 * Considera clientes de qualquer gerente.
 */
export abstract class TopClientesRepository {
  abstract obterTop3(): TopCliente[];
}
