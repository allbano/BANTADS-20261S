import type { Gerente } from '../models/gerente.model';

/**
 * Contrato abstrato do repositório de Gerentes para o Administrador.
 *
 * Define as operações de CRUD (R17–R20) e listagem (R19).
 * A camada de domínio define o "o quê", a infraestrutura define o "como".
 */
export abstract class GerenteAdminRepository {
  /** R19: Lista todos os gerentes. */
  abstract listarTodos(): Gerente[];

  /** Busca gerente pelo ID. */
  abstract buscarPorId(id: number): Gerente | undefined;

  /** R17: Insere um novo gerente. */
  abstract inserir(gerente: Gerente): void;

  /** R20: Atualiza dados de um gerente (nome, email, senha). */
  abstract atualizar(gerente: Gerente): void;

  /** R18: Remove um gerente pelo ID. */
  abstract remover(id: number): void;
}
