import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, map, of } from 'rxjs';

import { environment } from '../../../../../environments/environment';
import type { ClienteRelatorio } from '../../domain/models/cliente-relatorio.model';
import { RelatorioAdminRepository } from '../../domain/repositories/relatorio-admin.repository';

interface RelatorioClienteResponse {
  cpf: string;
  nome: string;
  email: string;
  salario: number | null;
  conta: string | null;
  saldo: number | null;
  limite: number | null;
  gerente: string | null;
  gerente_nome: string | null;
}

/**
 * R16 — Relatório de clientes via composição no gateway
 * (GET /clientes?filtro=adm_relatorio_clientes).
 */
@Injectable({
  providedIn: 'root',
})
export class RelatorioAdminHttpService extends RelatorioAdminRepository {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  override listarClientes(): Observable<ClienteRelatorio[]> {
    return this.http
      .get<RelatorioClienteResponse[]>(`${this.base}/clientes`, {
        params: { filtro: 'adm_relatorio_clientes' },
      })
      .pipe(
        map((lista) =>
          (lista ?? []).map((c) => ({
            cpf: c.cpf,
            nome: c.nome,
            email: c.email,
            salario: c.salario ?? 0,
            numeroConta: c.conta ?? '',
            saldo: c.saldo ?? 0,
            limite: c.limite ?? 0,
            cpfGerente: c.gerente ?? '',
            nomeGerente: c.gerente_nome ?? '',
          })),
        ),
        catchError(() => of<ClienteRelatorio[]>([])),
      );
  }
}
