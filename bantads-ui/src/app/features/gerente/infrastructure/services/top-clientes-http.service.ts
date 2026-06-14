import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, map, of } from 'rxjs';

import { environment } from '../../../../../environments/environment';
import type { TopCliente } from '../../domain/models/top-cliente.model';
import { TopClientesRepository } from '../../domain/repositories/top-clientes.repository';

interface MelhorClienteResponse {
  cpf: string;
  nome: string;
  cidade: string | null;
  estado: string | null;
  saldo: number | null;
}

@Injectable({
  providedIn: 'root',
})
export class TopClientesHttpService extends TopClientesRepository {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  override obterTop3(): Observable<TopCliente[]> {
    return this.http
      .get<MelhorClienteResponse[]>(`${this.base}/clientes`, { params: { filtro: 'melhores_clientes' } })
      .pipe(
        map((lista) =>
          (lista ?? []).map((c) => ({
            cpf: c.cpf,
            nome: c.nome,
            cidade: c.cidade ?? '',
            estado: c.estado ?? '',
            saldo: c.saldo ?? 0,
          })),
        ),
        catchError(() => of<TopCliente[]>([])),
      );
  }
}
