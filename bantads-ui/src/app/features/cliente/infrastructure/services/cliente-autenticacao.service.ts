import { Injectable, inject } from '@angular/core';

import type { Cliente } from '../../domain/models/cliente.model';
import { CLIENTES_DEMONSTRACAO } from '../data/clientes-demonstracao';
import { ClienteLocalStorageService } from './cliente-local-storage.service';

/**
 * Autenticação local até existir API (R2).
 */
@Injectable({
  providedIn: 'root',
})
export class ClienteAutenticacaoService {
  private readonly armazenamento = inject(ClienteLocalStorageService);

  buscarPorCredencial(email: string, senha: string): Cliente | null {
    const map = new Map<number, Cliente>();
    for (const c of CLIENTES_DEMONSTRACAO) {
      map.set(c.id, c);
    }
    for (const c of this.armazenamento.listarTodos()) {
      map.set(c.id, c);
    }
    const norm = email.trim().toLowerCase();
    for (const c of map.values()) {
      if (c.email.toLowerCase() === norm && c.senha === senha) {
        return c;
      }
    }
    return null;
  }
}
