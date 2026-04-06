import type { PerfilCliente, AlterarSenhaPayload } from '../models/perfil-cliente.model';
import type { ResultadoOperacao } from '../models/resultado-operacao.model';

/**
 * Contrato abstrato do repositório de Perfil do Cliente.
 *
 * A camada de domínio define o "o quê" — a infraestrutura define o "como".
 * Hoje: mock com localStorage. Futuramente: HTTP API.
 */
export abstract class PerfilClienteRepository {
  abstract buscarPerfil(clienteId: number): PerfilCliente | null;
  abstract salvarPerfil(perfil: PerfilCliente): ResultadoOperacao;
  abstract alterarSenha(payload: AlterarSenhaPayload): ResultadoOperacao;
}
