import { Injectable, computed, signal } from '@angular/core';

const LS_CHAVE = 'bantads_sessao';

export type TipoUsuario = 'CLIENTE' | 'GERENTE' | 'ADMIN';

/**
 * Dados da sessão autenticada, persistidos em localStorage.
 * Substitui os antigos sessao-cliente/gerente/admin (que guardavam apenas um id numérico).
 *
 * - `token`: JWT emitido pela API Gateway (enviado em Authorization: Bearer).
 * - `cpf`: identidade do usuário no backend (string).
 * - `numeroConta`: número da conta do cliente (necessário às operações /contas/{numero}/*).
 */
export interface SessaoUsuario {
  token: string;
  tipo: TipoUsuario;
  cpf: string | null;
  nome: string | null;
  email: string;
  numeroConta?: string | null;
}

@Injectable({
  providedIn: 'root',
})
export class SessaoService {
  private readonly _sessao = signal<SessaoUsuario | null>(this.lerArmazenado());

  readonly sessao = this._sessao.asReadonly();
  readonly estaAutenticado = computed(() => this._sessao() !== null);
  readonly tipo = computed<TipoUsuario | null>(() => this._sessao()?.tipo ?? null);
  readonly cpf = computed<string | null>(() => this._sessao()?.cpf ?? null);
  readonly nome = computed<string | null>(() => this._sessao()?.nome ?? null);
  readonly numeroConta = computed<string | null>(() => this._sessao()?.numeroConta ?? null);

  iniciar(sessao: SessaoUsuario): void {
    this._sessao.set(sessao);
    localStorage.setItem(LS_CHAVE, JSON.stringify(sessao));
  }

  /** Atualiza campos pontuais da sessão (ex.: numeroConta do cliente após login). */
  atualizar(parcial: Partial<SessaoUsuario>): void {
    const atual = this._sessao();
    if (!atual) {
      return;
    }
    const nova = { ...atual, ...parcial };
    this._sessao.set(nova);
    localStorage.setItem(LS_CHAVE, JSON.stringify(nova));
  }

  encerrar(): void {
    this._sessao.set(null);
    localStorage.removeItem(LS_CHAVE);
  }

  token(): string | null {
    return this._sessao()?.token ?? null;
  }

  private lerArmazenado(): SessaoUsuario | null {
    const raw = localStorage.getItem(LS_CHAVE);
    if (!raw) {
      return null;
    }
    try {
      const p = JSON.parse(raw) as SessaoUsuario;
      return typeof p.token === 'string' && typeof p.tipo === 'string' ? p : null;
    } catch {
      return null;
    }
  }
}
