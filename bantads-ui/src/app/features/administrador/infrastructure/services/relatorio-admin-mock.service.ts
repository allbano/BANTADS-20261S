import { Injectable, inject } from '@angular/core';

import type { ClienteRelatorio } from '../../domain/models/cliente-relatorio.model';
import { RelatorioAdminRepository } from '../../domain/repositories/relatorio-admin.repository';
import { ClienteContaMockService } from '../../../cliente/infrastructure/services/cliente-conta-mock.service';
import { CLIENTES_DEMONSTRACAO } from '../../../cliente/infrastructure/data/clientes-demonstracao';
import { GERENTES_MOCK } from '../data/gerentes-mock';

/**
 * Mapeamento fixo cliente → gerente (extraído das imagens "Conta").
 */
const MAPA_CLIENTE_GERENTE: Record<number, number> = {
  1: 1, // Catharyna  → Geniéve
  2: 2, // Cleuddônio → Godophredo
  3: 3, // Catianna   → Gyândula
  4: 1, // Cutardo    → Geniéve
  5: 2, // Coândrya   → Godophredo
};

/**
 * Implementação mock do RelatorioAdminRepository (R16).
 *
 * Monta a lista completa de clientes cruzando:
 * - CLIENTES_DEMONSTRACAO (nome, cpf, email, salario)
 * - ClienteContaMockService (numeroConta, saldo, limite)
 * - GERENTES_MOCK (cpf e nome do gerente)
 */
@Injectable({ providedIn: 'root' })
export class RelatorioAdminMockService extends RelatorioAdminRepository {
  private readonly contaService = inject(ClienteContaMockService);

  override listarClientes(): ClienteRelatorio[] {
    const resultado: ClienteRelatorio[] = [];

    for (const cliente of CLIENTES_DEMONSTRACAO) {
      const resumo = this.contaService.obterResumo(cliente.id);
      if (!resumo) continue;

      const gerenteId = MAPA_CLIENTE_GERENTE[cliente.id];
      const gerente = GERENTES_MOCK.find(g => g.id === gerenteId);

      resultado.push({
        cpf: String(cliente.cpf),
        nome: cliente.nome,
        email: cliente.email,
        salario: cliente.salario,
        numeroConta: resumo.numeroConta,
        saldo: resumo.saldo,
        limite: resumo.limiteCredito,
        cpfGerente: gerente?.cpf ?? '',
        nomeGerente: gerente?.nome ?? '',
      });
    }

    return resultado;
  }
}
