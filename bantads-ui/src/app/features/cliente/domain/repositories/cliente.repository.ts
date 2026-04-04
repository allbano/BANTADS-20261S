import { Cliente } from '../models/cliente.model';

/**
 * Contrato abstrato do repositório de Cliente.
 *
 * Segue o mesmo padrão do AutocadastroRepository —
 * a camada de domínio define o "o quê", a infraestrutura define o "como".
 */
export abstract class ClienteRepository {
  abstract listarTodos(): Cliente[];
  abstract buscarPorCPF(cpf: number): Cliente | undefined;
  abstract inserir(cliente: Cliente): void;
  abstract atualizar(cliente: Cliente): void;
  abstract remover(id: number): void;
}
