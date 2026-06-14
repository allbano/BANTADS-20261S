import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, map, of } from 'rxjs';

import { environment } from '../../../../../environments/environment';
import type { ClienteGerente } from '../../domain/models/cliente-gerente.model';
import { ClientesGerenteRepository } from '../../domain/repositories/clientes-gerente.repository';

interface ClienteContaResponse {
  cpf: string;
  nome: string;
  email: string;
  telefone: string | null;
  endereco: string | null;
  cidade: string | null;
  estado: string | null;
  salario?: number | null;
  conta: string | null;
  saldo: number | null;
  limite: number | null;
}

@Injectable({
  providedIn: 'root',
})
export class ClientesGerenteHttpService extends ClientesGerenteRepository {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  override listarTodos(): Observable<ClienteGerente[]> {
    return this.http.get<ClienteContaResponse[]>(`${this.base}/clientes`).pipe(
      map((lista) => (lista ?? []).map((c) => this.mapCliente(c))),
      catchError(() => of<ClienteGerente[]>([])),
    );
  }

  override buscarPorCpf(cpf: string): Observable<ClienteGerente | null> {
    const normalizado = cpf.replace(/\D/g, '');
    return this.http.get<ClienteContaResponse>(`${this.base}/clientes/${normalizado}`).pipe(
      map((c) => this.mapCliente(c)),
      catchError(() => of(null)),
    );
  }

  private mapCliente(c: ClienteContaResponse): ClienteGerente {
    return {
      nome: c.nome,
      cpf: c.cpf,
      email: c.email,
      telefone: c.telefone ?? '',
      salario: c.salario ?? 0,
      endereco: {
        cep: '',
        logradouro: c.endereco ?? '',
        numero: '',
        complemento: '',
        cidade: c.cidade ?? '',
        uf: c.estado ?? '',
      },
      numeroConta: c.conta ?? '',
      saldo: c.saldo ?? 0,
      limite: c.limite ?? 0,
      dataAberturaConta: '',
    };
  }
}
