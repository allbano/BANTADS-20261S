import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, map, of } from 'rxjs';

import { environment } from '../../../../../environments/environment';
import { mensagemDeErro } from '../../../../core/http/error.interceptor';
import type { PedidoAutocadastro } from '../../domain/models/pedido-autocadastro.model';
import type { ResultadoAprovacao } from '../../domain/models/resultado-aprovacao.model';
import { AprovacaoRepository } from '../../domain/repositories/aprovacao.repository';

interface ClientePendenteResponse {
  cpf: string;
  nome: string;
  email: string;
  telefone: string | null;
  salario: number | null;
  endereco: string | null;
  cidade: string | null;
  estado: string | null;
  dataSolicitacao?: string;
}

@Injectable({
  providedIn: 'root',
})
export class AprovacaoHttpService extends AprovacaoRepository {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  override listarPendentes(): Observable<PedidoAutocadastro[]> {
    return this.http
      .get<ClientePendenteResponse[]>(`${this.base}/clientes`, { params: { filtro: 'para_aprovar' } })
      .pipe(
        map((lista) => (lista ?? []).map((c) => this.mapPedido(c))),
        catchError(() => of<PedidoAutocadastro[]>([])),
      );
  }

  override aprovar(cpf: string): Observable<ResultadoAprovacao> {
    return this.http.post(`${this.base}/clientes/${cpf}/aprovar`, {}).pipe(
      map(() => ({ sucesso: true, mensagem: 'Cliente aprovado! Conta criada e e-mail enviado.' }) as ResultadoAprovacao),
      catchError((err: HttpErrorResponse) =>
        of<ResultadoAprovacao>({ sucesso: false, mensagem: mensagemDeErro(err, 'Não foi possível aprovar o cliente.') }),
      ),
    );
  }

  override rejeitar(cpf: string, motivo: string): Observable<ResultadoAprovacao> {
    if (!motivo.trim()) {
      return of({ sucesso: false, mensagem: 'O motivo da rejeição é obrigatório.' });
    }
    return this.http.post(`${this.base}/clientes/${cpf}/rejeitar`, { motivo }).pipe(
      map(() => ({ sucesso: true, mensagem: 'Pedido recusado. E-mail enviado ao cliente.' }) as ResultadoAprovacao),
      catchError((err: HttpErrorResponse) =>
        of<ResultadoAprovacao>({ sucesso: false, mensagem: mensagemDeErro(err, 'Não foi possível recusar o pedido.') }),
      ),
    );
  }

  private mapPedido(c: ClientePendenteResponse): PedidoAutocadastro {
    return {
      cpf: c.cpf,
      nome: c.nome,
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
      dataSolicitacao: c.dataSolicitacao,
    };
  }
}
