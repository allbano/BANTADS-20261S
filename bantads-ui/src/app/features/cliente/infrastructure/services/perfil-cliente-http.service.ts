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
  cep: string | null;
  cidade: string;
  estado: string;
  salario: number;
}

/**
 * DTO de PUT /clientes/{cpf} (R4 — SAGA de alteração de perfil).
 * O ms-cliente consome o corpo como Map e lê a chave `cep` (minúsculo)
 * — diferente do autocadastro, que aceita `CEP` via @JsonAlias.
 */
interface PerfilRequest {
  nome: string;
  email: string;
  salario: number;
  endereco: string;
  cep: string;
  cidade: string;
  estado: string;
}

@Injectable({
  providedIn: 'root',
})
/** R4 — Acesso HTTP do perfil: GET /clientes/{cpf} e PUT /clientes/{cpf} (SAGA) na API Gateway. */
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
      map((dados) => {
        // A Gateway entrega o endereço como string única; separamos
        // logradouro/número/complemento para os campos do formulário.
        const { logradouro, numero, complemento } = this.parseEndereco(dados.endereco);
        return {
          cpf: dados.cpf,
          nome: dados.nome,
          email: dados.email,
          salario: dados.salario ?? 0,
          telefone: dados.telefone ?? '',
          endereco: {
            cep: dados.cep ?? '',
            logradouro,
            numero,
            complemento,
            cidade: dados.cidade ?? '',
            uf: dados.estado ?? '',
          },
        };
      }),
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
      cep: perfil.endereco.cep,
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

  /**
   * Separa a string única de endereço em logradouro/número/complemento.
   * Cobre os dois formatos que o sistema produz:
   *   - seed/legado:  "Rua X, 123 - Apto 4"   (complemento após " - ")
   *   - gravado aqui: "Rua X, 123, Apto 4"    (separador vírgula)
   */
  private parseEndereco(raw: string | null): {
    logradouro: string;
    numero: string;
    complemento: string;
  } {
    let resto = (raw ?? '').trim();
    let complemento = '';

    // Complemento no formato legado: "... - complemento".
    const traco = resto.indexOf(' - ');
    if (traco >= 0) {
      complemento = resto.slice(traco + 3).trim();
      resto = resto.slice(0, traco).trim();
    }

    const partes = resto
      .split(',')
      .map((p) => p.trim())
      .filter((p) => p.length > 0);

    if (partes.length === 0) {
      return { logradouro: '', numero: '', complemento };
    }
    if (partes.length === 1) {
      return { logradouro: partes[0], numero: '', complemento };
    }

    const logradouro = partes[0];
    const numero = partes[1];
    // Partes extras (formato vírgula com complemento) só viram complemento
    // se ainda não tivermos capturado um pelo " - ".
    if (partes.length > 2 && !complemento) {
      complemento = partes.slice(2).join(', ');
    }
    return { logradouro, numero, complemento };
  }

  private formatarLogradouro(perfil: PerfilCliente): string {
    const { logradouro, numero, complemento } = perfil.endereco;
    return [logradouro, numero, complemento]
      .map((p) => p?.trim())
      .filter((p): p is string => !!p)
      .join(', ');
  }
}
