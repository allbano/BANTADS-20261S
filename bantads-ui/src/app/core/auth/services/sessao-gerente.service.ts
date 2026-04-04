import { Injectable, computed, signal } from '@angular/core';

const LS_CHAVE = 'bantads_sessao_gerente';

interface SessaoPayload {
  gerenteId: number;
}

@Injectable({
  providedIn: 'root',
})
export class SessaoGerenteService {
  private readonly _gerenteId = signal<number | null>(this.lerArmazenado());

  readonly gerenteId = this._gerenteId.asReadonly();
  readonly estaAutenticado = computed(() => this._gerenteId() !== null);

  iniciar(gerenteId: number): void {
    this._gerenteId.set(gerenteId);
    const payload: SessaoPayload = { gerenteId };
    localStorage.setItem(LS_CHAVE, JSON.stringify(payload));
  }

  encerrar(): void {
    this._gerenteId.set(null);
    localStorage.removeItem(LS_CHAVE);
  }

  private lerArmazenado(): number | null {
    const raw = localStorage.getItem(LS_CHAVE);
    if (!raw) {
      return null;
    }
    try {
      const p = JSON.parse(raw) as SessaoPayload;
      return typeof p.gerenteId === 'number' ? p.gerenteId : null;
    } catch {
      return null;
    }
  }
}
