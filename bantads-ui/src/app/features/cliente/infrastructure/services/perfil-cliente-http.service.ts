import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, map, of, throwError } from 'rxjs';

import { environment } from '../../../../../environments/environment';
import { mensagemDeErro } from '../../../../core/http/error.interceptor';
import { SessaoService } from '../../../../core/auth/services/sessao.service';
import type { PerfilCliente } from '../../domain/models/perfil-cliente.model';
import type { ResultadoOperacao } from '../../domain/models/resultado-operacao.model';
import { PerfilClienteRepository } from '../../domain/repositories/perfil-cliente.repository';

interface DadosClienteResponse {
  cpf: string;
  nome: string;
  telefone: string | null;
  email: string;
  endereco: string;
  cidade: string;
  estado: string;
  salario: number;
}

/** DTO de PUT /clientes/{cpf} (R4 — SAGA de alteração de perfil). */
interface PerfilRequest {
  nome: string;
  email: string;
  salario: number;
  endereco: string;
  CEP: string;
  cidade: string;
  estado: string;
}

@Injectable({
  providedIn: 'root',
})
export class PerfilClienteHttpService extends PerfilClienteRepository {
  private readonly http = inject(HttpClient);
  private readonly sessao = inject(SessaoService);
  private readonly base = environment.apiUrl;

  override buscarPerfil(): Observable<PerfilCliente> {
    const cpf = this.sessao.cpf();
    if (!cpf) {
      return throwError(() => new Error('Sessão sem CPF.'));
    }
    return this.http.get<DadosClienteResponse>(`${this.base}/clientes/${cpf}`).pipe(
      map((dados) => ({
        cpf: dados.cpf,
        nome: dados.nome,
        email: dados.email,
        salario: dados.salario ?? 0,
        telefone: dados.telefone ?? '',
        endereco: {
          // A Gateway entrega o endereço como string única; CEP/número ficam em branco para reedição.
          cep: '',
          logradouro: dados.endereco ?? '',
          numero: '',
          complemento: '',
          cidade: dados.cidade ?? '',
          uf: dados.estado ?? '',
        },
      })),
    );
  }

  override salvarPerfil(perfil: PerfilCliente): Observable<ResultadoOperacao> {
    const cpf = perfil.cpf || this.sessao.cpf();
    if (!cpf) {
      return of({ sucesso: false, mensagem: 'Sessão sem CPF.' });
    }
    const body: PerfilRequest = {
      nome: perfil.nome,
      email: perfil.email,
      salario: perfil.salario,
      endereco: this.formatarLogradouro(perfil),
      CEP: perfil.endereco.cep,
      cidade: perfil.endereco.cidade,
      estado: perfil.endereco.uf,
    };
    return this.http.put(`${this.base}/clientes/${cpf}`, body).pipe(
      map(() => ({ sucesso: true, mensagem: 'Perfil atualizado com sucesso.' })),
      catchError((err: HttpErrorResponse) =>
        of({ sucesso: false, mensagem: mensagemDeErro(err, 'Não foi possível salvar o perfil.') }),
      ),
    );
  }

  private formatarLogradouro(perfil: PerfilCliente): string {
    const { logradouro, numero, complemento } = perfil.endereco;
    return [logradouro, numero, complemento]
      .map((p) => p?.trim())
      .filter((p): p is string => !!p)
      .join(', ');
  }
}
