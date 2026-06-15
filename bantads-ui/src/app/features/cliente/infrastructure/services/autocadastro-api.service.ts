import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, map, throwError } from 'rxjs';

import { environment } from '../../../../../environments/environment';
import { AutocadastroRepository } from '../../domain/repositories/autocadastro.repository';
import { AutocadastroPayload, Endereco } from '../../domain/models/autocadastro.model';

/** DTO plano esperado pela API Gateway em POST /clientes (R1 — SAGA de autocadastro). */
interface AutocadastroRequest {
  cpf: string;
  email: string;
  nome: string;
  telefone: string;
  salario: number;
  endereco: string;
  CEP: string;
  cidade: string;
  estado: string;
}

@Injectable({
  providedIn: 'root',
})
/** R1 — Acesso HTTP do autocadastro: POST /clientes (público) na API Gateway → SAGA. */
export class AutocadastroApiService extends AutocadastroRepository {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  override solicitarCadastro(payload: AutocadastroPayload): Observable<void> {
    const body = this.montarRequest(payload);
    return this.http.post(`${this.base}/clientes`, body).pipe(
      map(() => void 0),
      catchError((err: HttpErrorResponse) => {
        const mensagem =
          err.status === 409
            ? 'Este CPF já está cadastrado ou em processo de aprovação.'
            : 'Não foi possível concluir o cadastro. Verifique os dados e tente novamente.';
        return throwError(() => new Error(mensagem));
      }),
    );
  }

  private montarRequest(payload: AutocadastroPayload): AutocadastroRequest {
    const { dadosPessoais, endereco } = payload;
    return {
      cpf: dadosPessoais.cpf.replace(/\D/g, ''),
      email: dadosPessoais.email,
      nome: dadosPessoais.nome,
      telefone: dadosPessoais.telefone,
      salario: dadosPessoais.salario,
      endereco: this.formatarLogradouro(endereco),
      CEP: endereco.cep,
      cidade: endereco.cidade,
      estado: endereco.uf,
    };
  }

  /** Junta logradouro, número e complemento em uma única string de endereço. */
  private formatarLogradouro(endereco: Endereco): string {
    const partes = [endereco.logradouro, endereco.numero, endereco.complemento]
      .map((p) => p?.trim())
      .filter((p): p is string => !!p);
    return partes.join(', ');
  }
}
