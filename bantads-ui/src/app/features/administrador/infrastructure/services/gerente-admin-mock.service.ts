import { Injectable } from '@angular/core';

import type { Gerente } from '../../domain/models/gerente.model';
import { GerenteAdminRepository } from '../../domain/repositories/gerente-admin.repository';
import { GERENTES_MOCK } from '../data/gerentes-mock';

/**
 * Implementação mock do GerenteAdminRepository (R17–R20).
 *
 * CRUD em memória. Quando a API estiver pronta,
 * basta criar um novo service que implemente o mesmo contrato via HTTP.
 */
@Injectable({ providedIn: 'root' })
export class GerenteAdminMockService extends GerenteAdminRepository {
  private gerentes: Gerente[] = [...GERENTES_MOCK];
  private idSeq = 100;

  override listarTodos(): Gerente[] {
    return [...this.gerentes];
  }

  override buscarPorId(id: number): Gerente | undefined {
    return this.gerentes.find(g => g.id === id);
  }

  override inserir(gerente: Gerente): void {
    const novo: Gerente = {
      ...gerente,
      id: this.idSeq++,
    };
    this.gerentes.push(novo);
  }

  override atualizar(gerente: Gerente): void {
    const idx = this.gerentes.findIndex(g => g.id === gerente.id);
    if (idx !== -1) {
      this.gerentes[idx] = { ...gerente };
    }
  }

  override remover(id: number): void {
    this.gerentes = this.gerentes.filter(g => g.id !== id);
  }
}
