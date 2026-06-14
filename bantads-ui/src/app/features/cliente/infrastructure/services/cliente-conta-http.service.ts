import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, forkJoin, map, of, throwError } from 'rxjs';

import { environment } from '../../../../../environments/environment';
import { mensagemDeErro } from '../../../../core/http/error.interceptor';
import { SessaoService } from '../../../../core/auth/services/sessao.service';
import type { DashboardClienteResumo } from '../../domain/models/dashboard-cliente-resumo.model';
import type { ExtratoDia } from '../../domain/models/extrato-dia.model';
import type { Movimentacao } from '../../domain/models/movimentacao.model';
import type { ResultadoOperacao } from '../../domain/models/resultado-operacao.model';
import type { TipoMovimentacao } from '../../domain/models/tipo-movimentacao';
import { ClienteContaRepository } from '../../domain/repositories/cliente-conta.repository';

interface DadosClienteResponse {
  cpf: string;
  nome: string;
  telefone: string | null;
  email: string;
  endereco: string;
  cidade: string;
  estado: string;
  salario: number;
  conta: string | null;
  saldo: number | null;
  limite: number | null;
  gerente: string | null;
  gerente_nome: string | null;
  gerente_email: string | null;
}

interface ItemExtratoResponse {
  data: string;
  tipo: 'saque' | 'depósito' | 'transferência';
  origem: string | null;
  origemNome: string | null;
  destino: string | null;
  destinoNome: string | null;
  valor: number;
}

interface ExtratoResponse {
  conta: string;
  saldo: number;
  movimentacoes: ItemExtratoResponse[];
}

interface OperacaoResponse {
  conta: string;
  data: string;
  saldo: number;
}

function formatarMoeda(valor: number): string {
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(valor);
}

@Injectable({
  providedIn: 'root',
})
export class ClienteContaHttpService extends ClienteContaRepository {
  private readonly http = inject(HttpClient);
  private readonly sessao = inject(SessaoService);
  private readonly base = environment.apiUrl;

  override obterResumo(): Observable<DashboardClienteResumo> {
    const cpf = this.sessao.cpf();
    const numero = this.sessao.numeroConta();
    if (!cpf || !numero) {
      return throwError(() => new Error('Sessão sem CPF ou número de conta.'));
    }
    return forkJoin({
      cliente: this.http.get<DadosClienteResponse>(`${this.base}/clientes/${cpf}`),
      extrato: this.http
        .get<ExtratoResponse>(`${this.base}/contas/${numero}/extrato`)
        .pipe(catchError(() => of<ExtratoResponse>({ conta: numero, saldo: 0, movimentacoes: [] }))),
    }).pipe(
      map(({ cliente, extrato }) => this.montarResumo(cliente, extrato, numero)),
    );
  }

  override depositar(valor: number): Observable<ResultadoOperacao> {
    return this.operacaoSimples('depositar', valor, 'Depósito realizado!');
  }

  override sacar(valor: number): Observable<ResultadoOperacao> {
    return this.operacaoSimples('sacar', valor, 'Saque realizado!');
  }

  override transferir(numeroContaDestino: string, valor: number): Observable<ResultadoOperacao> {
    const numero = this.sessao.numeroConta();
    if (!numero) {
      return of({ sucesso: false, mensagem: 'Conta não encontrada na sessão.' });
    }
    if (!Number.isFinite(valor) || valor <= 0) {
      return of({ sucesso: false, mensagem: 'Informe um valor válido.' });
    }
    const destino = numeroContaDestino.replace(/\D/g, '');
    if (!destino) {
      return of({ sucesso: false, mensagem: 'Informe a conta de destino.' });
    }
    return this.http
      .post<OperacaoResponse>(`${this.base}/contas/${numero}/transferir`, { destino, valor })
      .pipe(
        map((resp) => ({
          sucesso: true,
          mensagem: `Transferência realizada! Novo saldo: ${formatarMoeda(resp.saldo)}`,
        })),
        catchError((err: HttpErrorResponse) =>
          of({ sucesso: false, mensagem: mensagemDeErro(err, 'Não foi possível concluir a transferência.') }),
        ),
      );
  }

  override consultarExtrato(dataInicioIso: string, dataFimIso: string): Observable<ExtratoDia[]> {
    const numero = this.sessao.numeroConta();
    if (!numero) {
      return of([]);
    }
    return this.http.get<ExtratoResponse>(`${this.base}/contas/${numero}/extrato`).pipe(
      map((extrato) => this.agruparPorDia(extrato, numero, dataInicioIso, dataFimIso)),
      catchError(() => of<ExtratoDia[]>([])),
    );
  }

  // ── Helpers ────────────────────────────────────────────────────────

  private operacaoSimples(
    rota: 'depositar' | 'sacar',
    valor: number,
    sucessoBase: string,
  ): Observable<ResultadoOperacao> {
    const numero = this.sessao.numeroConta();
    if (!numero) {
      return of({ sucesso: false, mensagem: 'Conta não encontrada na sessão.' });
    }
    if (!Number.isFinite(valor) || valor <= 0) {
      return of({ sucesso: false, mensagem: 'Informe um valor válido.' });
    }
    return this.http
      .post<OperacaoResponse>(`${this.base}/contas/${numero}/${rota}`, { valor })
      .pipe(
        map((resp) => ({ sucesso: true, mensagem: `${sucessoBase} Novo saldo: ${formatarMoeda(resp.saldo)}` })),
        catchError((err: HttpErrorResponse) =>
          of({ sucesso: false, mensagem: mensagemDeErro(err, 'Não foi possível concluir a operação.') }),
        ),
      );
  }

  private montarResumo(
    cliente: DadosClienteResponse,
    extrato: ExtratoResponse,
    numero: string,
  ): DashboardClienteResumo {
    const movimentacoes = extrato.movimentacoes
      .map((item, idx) => this.mapMovimentacao(item, numero, idx))
      .sort((a, b) => b.instante.localeCompare(a.instante));

    return {
      cpf: cliente.cpf,
      nomeCliente: cliente.nome,
      emailCliente: cliente.email,
      numeroConta: cliente.conta ?? numero,
      saldo: cliente.saldo ?? extrato.saldo ?? 0,
      salario: cliente.salario ?? 0,
      limiteCredito: cliente.limite ?? 0,
      gerente: {
        nome: cliente.gerente_nome ?? '—',
        email: cliente.gerente_email ?? '',
      },
      dataAberturaConta: '',
      ultimasMovimentacoes: movimentacoes.slice(0, 5),
    };
  }

  private agruparPorDia(
    extrato: ExtratoResponse,
    numero: string,
    inicioIso: string,
    fimIso: string,
  ): ExtratoDia[] {
    const movs = extrato.movimentacoes
      .map((item, idx) => this.mapMovimentacao(item, numero, idx))
      .filter((m) => {
        const dia = m.instante.slice(0, 10);
        return dia >= inicioIso && dia <= fimIso;
      })
      .sort((a, b) => a.instante.localeCompare(b.instante));

    const porDia = new Map<string, Movimentacao[]>();
    for (const m of movs) {
      const dia = m.instante.slice(0, 10);
      const lista = porDia.get(dia) ?? [];
      lista.push(m);
      porDia.set(dia, lista);
    }

    // Saldo final do dia é aproximado a partir do saldo atual da conta retrocedendo movimentações.
    const saldoAtual = extrato.saldo ?? 0;
    let acumulado = saldoAtual;
    const dias = [...porDia.keys()].sort();
    const resultado: ExtratoDia[] = [];
    // Percorre do mais recente ao mais antigo para reconstruir o saldo de fim de cada dia.
    for (let i = dias.length - 1; i >= 0; i--) {
      const dia = dias[i];
      const movsDia = porDia.get(dia)!;
      resultado.unshift({ data: dia, movimentacoes: movsDia, saldoConsolidadoFimDia: acumulado });
      for (const m of movsDia) {
        acumulado -= m.sentido === 'entrada' ? m.valor : -m.valor;
      }
    }
    return resultado;
  }

  private mapMovimentacao(item: ItemExtratoResponse, numeroProprio: string, idx: number): Movimentacao {
    const tipo = this.mapTipo(item.tipo);
    let sentido: 'entrada' | 'saida';
    let contraparteLabel = '';

    if (tipo === 'deposito') {
      sentido = 'entrada';
    } else if (tipo === 'saque') {
      sentido = 'saida';
    } else {
      const ehOrigem = item.origem === numeroProprio;
      sentido = ehOrigem ? 'saida' : 'entrada';
      if (ehOrigem) {
        contraparteLabel = this.rotuloConta(item.destinoNome, item.destino);
      } else {
        contraparteLabel = this.rotuloConta(item.origemNome, item.origem);
      }
    }

    return {
      id: `${item.data}-${idx}`,
      instante: item.data,
      tipo,
      sentido,
      valor: item.valor,
      contraparteLabel,
    };
  }

  private mapTipo(tipo: ItemExtratoResponse['tipo']): TipoMovimentacao {
    switch (tipo) {
      case 'depósito':
        return 'deposito';
      case 'saque':
        return 'saque';
      case 'transferência':
        return 'transferencia';
    }
  }

  private rotuloConta(nome: string | null, numero: string | null): string {
    if (nome && numero) {
      return `${nome} (${numero})`;
    }
    return nome ?? (numero ? `Conta ${numero}` : '');
  }
}
