import type { Observable } from 'rxjs';

import type { PedidoAutocadastro } from '../models/pedido-autocadastro.model';
import type { ResultadoAprovacao } from '../models/resultado-aprovacao.model';

/**
 * Contrato do repositório de aprovação/rejeição de autocadastros (R9/R10/R11).
 * A identidade do pedido é o CPF do cliente.
 */
export abstract class AprovacaoRepository {
  /** Lista os clientes aguardando aprovação (R9). */
  abstract listarPendentes(): Observable<PedidoAutocadastro[]>;

  /** Aprova o cadastro: cria conta e dispara e-mail com a senha (R10 — SAGA). */
  abstract aprovar(cpf: string): Observable<ResultadoAprovacao>;

  /** Rejeita o cadastro com motivo e dispara e-mail (R11). */
  abstract rejeitar(cpf: string, motivo: string): Observable<ResultadoAprovacao>;
}
