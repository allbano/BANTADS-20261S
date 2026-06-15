import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, map, of } from 'rxjs';

import { environment } from '../../../../../environments/environment';
import { mensagemDeErro } from '../../../../core/http/error.interceptor';
import type { Gerente, GerenteAlteracao, GerenteInsercao } from '../../domain/models/gerente.model';
import { GerenteAdminRepository, ResultadoGerente } from '../../domain/repositories/gerente-admin.repository';

interface DadoGerente {
  cpf: string;
  nome: string;
  email: string;
  tipo: string;
  telefone?: string;
}

@Injectable({
  providedIn: 'root',
})
/** Acesso HTTP ao CRUD de gerentes (admin): inserir (R17), remover (R18),
 *  listar (R19) e alterar (R20) via API Gateway → SAGA. */
export class GerenteAdminHttpService extends GerenteAdminRepository {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  override listarTodos(): Observable<Gerente[]> {
    return this.http.get<DadoGerente[]>(`${this.base}/gerentes`).pipe(
      map((lista) =>
        (lista ?? []).map((g) => ({
          cpf: g.cpf,
          nome: g.nome,
          email: g.email,
          telefone: g.telefone ?? '',
          tipo: g.tipo,
        })),
      ),
      catchError(() => of<Gerente[]>([])),
    );
  }

  override inserir(gerente: GerenteInsercao): Observable<ResultadoGerente> {
    return this.http.post(`${this.base}/gerentes`, gerente).pipe(
      map(() => ({ sucesso: true, mensagem: 'Gerente cadastrado com sucesso!' })),
      catchError((err: HttpErrorResponse) =>
        of<ResultadoGerente>({ sucesso: false, mensagem: mensagemDeErro(err, 'Não foi possível cadastrar o gerente.') }),
      ),
    );
  }

  override atualizar(cpf: string, dados: GerenteAlteracao): Observable<ResultadoGerente> {
    return this.http.put(`${this.base}/gerentes/${cpf}`, dados).pipe(
      map(() => ({ sucesso: true, mensagem: 'Dados do gerente atualizados com sucesso!' })),
      catchError((err: HttpErrorResponse) =>
        of<ResultadoGerente>({ sucesso: false, mensagem: mensagemDeErro(err, 'Não foi possível atualizar o gerente.') }),
      ),
    );
  }

  override remover(cpf: string): Observable<ResultadoGerente> {
    return this.http.delete(`${this.base}/gerentes/${cpf}`).pipe(
      map(() => ({ sucesso: true, mensagem: 'Gerente excluído com sucesso.' })),
      catchError((err: HttpErrorResponse) =>
        of<ResultadoGerente>({ sucesso: false, mensagem: mensagemDeErro(err, 'Não foi possível excluir o gerente.') }),
      ),
    );
  }
}
