import type { Observable } from 'rxjs';

import type { PerfilCliente } from '../models/perfil-cliente.model';
import type { ResultadoOperacao } from '../models/resultado-operacao.model';

/**
 * Contrato abstrato do repositório de Perfil do Cliente (R4).
 *
 * A camada de domínio define o "o quê" — a infraestrutura define o "como" (HTTP/Gateway).
 * A API Gateway não expõe troca de senha para clientes, portanto não há `alterarSenha`.
 */
export abstract class PerfilClienteRepository {
  abstract buscarPerfil(): Observable<PerfilCliente>;
  abstract salvarPerfil(perfil: PerfilCliente): Observable<ResultadoOperacao>;
}
