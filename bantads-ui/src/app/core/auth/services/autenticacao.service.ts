import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, map, of, switchMap, tap } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { SessaoService, SessaoUsuario, TipoUsuario } from './sessao.service';

interface LoginResponse {
  access_token: string;
  token_type: string;
  /** A Gateway devolve "ADMINISTRADOR" (contrato do testador DAC); aqui normalizamos para "ADMIN". */
  tipo: string;
  usuario: {
    nome: string | null;
    cpf: string | null;
    email: string;
  };
}

/** Mapeia o tipo cru da Gateway para o TipoUsuario do frontend (ADMINISTRADOR → ADMIN, FUNCIONARIO → GERENTE). */
function normalizarTipo(tipo: string): TipoUsuario {
  switch (tipo) {
    case 'ADMINISTRADOR':
      return 'ADMIN';
    case 'FUNCIONARIO':
      return 'GERENTE';
    default:
      return tipo as TipoUsuario;
  }
}

/** Resposta de GET /clientes/{cpf} — usada só para descobrir o numeroConta no login do cliente. */
interface DadosClienteResponse {
  conta: string | null;
}

/**
 * Autenticação contra a API Gateway (POST /login, POST /logout).
 * Para clientes, enriquece a sessão com o número da conta (necessário às operações /contas/{numero}/*).
 */
@Injectable({
  providedIn: 'root',
})
/** R2 — Login/Logout: autentica (POST /login), enriquece e persiste a sessão
 *  (token JWT + tipo + usuário) e encerra a sessão (POST /logout). */
export class AutenticacaoService {
  private readonly http = inject(HttpClient);
  private readonly sessao = inject(SessaoService);
  private readonly base = environment.apiUrl;

  login(login: string, senha: string): Observable<SessaoUsuario> {
    return this.http.post<LoginResponse>(`${this.base}/login`, { login, senha }).pipe(
      switchMap((resp) => {
        const tipo = normalizarTipo(resp.tipo);
        const sessao: SessaoUsuario = {
          token: resp.access_token,
          tipo,
          cpf: resp.usuario?.cpf ?? null,
          nome: resp.usuario?.nome ?? null,
          email: resp.usuario?.email,
          numeroConta: null,
        };
        this.sessao.iniciar(sessao);

        // Cliente: descobrir o número da conta para habilitar as operações de conta.
        if (tipo === 'CLIENTE' && sessao.cpf) {
          return this.http.get<DadosClienteResponse>(`${this.base}/clientes/${sessao.cpf}`).pipe(
            tap((dados) => this.sessao.atualizar({ numeroConta: dados?.conta ?? null })),
            map(() => this.sessao.sessao() ?? sessao),
            catchError(() => of(sessao)),
          );
        }
        return of(sessao);
      }),
    );
  }

  logout(): Observable<void> {
    return this.http.post(`${this.base}/logout`, {}).pipe(
      map(() => void 0),
      catchError(() => of(void 0)),
      tap(() => this.sessao.encerrar()),
    );
  }
}
