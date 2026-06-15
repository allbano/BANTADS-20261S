import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, map, of } from 'rxjs';

import { environment } from '../../../../../environments/environment';
import type { GerenteDashboard } from '../../domain/models/gerente-dashboard.model';
import { DashboardAdminRepository } from '../../domain/repositories/dashboard-admin.repository';

interface DadoGerente {
  cpf: string;
  nome: string;
  email: string;
  tipo: string;
}

interface ItemDashboardResponse {
  gerente: DadoGerente;
  clientes: unknown[];
  saldo_positivo: number;
  saldo_negativo: number;
}

/**
 * R15 — Dashboard do administrador via API Composition no gateway
 * (GET /gerentes?numero=dashboard).
 */
@Injectable({
  providedIn: 'root',
})
export class DashboardAdminHttpService extends DashboardAdminRepository {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  override obterEstatisticas(): Observable<GerenteDashboard[]> {
    return this.http
      .get<ItemDashboardResponse[]>(`${this.base}/gerentes`, { params: { filtro: 'dashboard' } })
      .pipe(
        map((itens) =>
          (itens ?? []).map((item) => ({
            gerenteCpf: item.gerente?.cpf ?? '',
            nome: item.gerente?.nome ?? '—',
            qtdClientes: item.clientes?.length ?? 0,
            saldoPositivo: item.saldo_positivo ?? 0,
            saldoNegativo: item.saldo_negativo ?? 0,
          })),
        ),
        catchError(() => of<GerenteDashboard[]>([])),
      );
  }
}
