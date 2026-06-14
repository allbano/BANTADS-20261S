import type { Observable } from 'rxjs';

import type { Gerente, GerenteAlteracao, GerenteInsercao } from '../models/gerente.model';

export interface ResultadoGerente {
  sucesso: boolean;
  mensagem: string;
}

/**
 * Contrato abstrato do repositório de Gerentes para o Administrador (R17–R20, R19).
 * A identidade é o CPF; a distribuição de contas é responsabilidade do backend (SAGA).
 */
export abstract class GerenteAdminRepository {
  /** R19: Lista todos os gerentes. */
  abstract listarTodos(): Observable<Gerente[]>;

  /** R17: Insere um novo gerente. */
  abstract inserir(gerente: GerenteInsercao): Observable<ResultadoGerente>;

  /** R20: Atualiza dados de um gerente (nome, email, senha). */
  abstract atualizar(cpf: string, dados: GerenteAlteracao): Observable<ResultadoGerente>;

  /** R18: Remove um gerente pelo CPF. */
  abstract remover(cpf: string): Observable<ResultadoGerente>;
}
