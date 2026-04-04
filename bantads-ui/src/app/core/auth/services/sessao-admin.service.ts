import { Injectable, computed, signal } from '@angular/core';

const LS_CHAVE = 'bantads_sessao_admin';

interface SessaoPayload {
  adminId: number;
}

@Injectable({
  providedIn: 'root',
})
export class SessaoAdminService {
  private readonly _adminId = signal<number | null>(this.lerArmazenado());

  readonly adminId = this._adminId.asReadonly();
  readonly estaAutenticado = computed(() => this._adminId() !== null);

  iniciar(adminId: number): void {
    this._adminId.set(adminId);
    const payload: SessaoPayload = { adminId };
    localStorage.setItem(LS_CHAVE, JSON.stringify(payload));
  }

  encerrar(): void {
    this._adminId.set(null);
    localStorage.removeItem(LS_CHAVE);
  }

  private lerArmazenado(): number | null {
    const raw = localStorage.getItem(LS_CHAVE);
    if (!raw) {
      return null;
    }
    try {
      const p = JSON.parse(raw) as SessaoPayload;
      return typeof p.adminId === 'number' ? p.adminId : null;
    } catch {
      return null;
    }
  }
}
