import { Injectable, computed, signal } from '@angular/core';

const LS_CHAVE = 'bantads_sessao_cliente';

interface SessaoPayload {
  clienteId: number;
}

@Injectable({
  providedIn: 'root',
})
export class SessaoClienteService {
  private readonly _clienteId = signal<number | null>(this.lerArmazenado());

  readonly clienteId = this._clienteId.asReadonly();
  readonly estaAutenticado = computed(() => this._clienteId() !== null);

  iniciar(clienteId: number): void {
    this._clienteId.set(clienteId);
    const payload: SessaoPayload = { clienteId };
    localStorage.setItem(LS_CHAVE, JSON.stringify(payload));
  }

  encerrar(): void {
    this._clienteId.set(null);
    localStorage.removeItem(LS_CHAVE);
  }

  private lerArmazenado(): number | null {
    const raw = localStorage.getItem(LS_CHAVE);
    if (!raw) {
      return null;
    }
    try {
      const p = JSON.parse(raw) as SessaoPayload;
      return typeof p.clienteId === 'number' ? p.clienteId : null;
    } catch {
      return null;
    }
  }
}
