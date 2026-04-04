import { Injectable } from '@angular/core';

import type { DashboardClienteResumo } from '../../domain/models/dashboard-cliente-resumo.model';
import type { ExtratoDia } from '../../domain/models/extrato-dia.model';
import type { GerenteResumo } from '../../domain/models/gerente-resumo.model';
import type { Movimentacao } from '../../domain/models/movimentacao.model';
import type { ResultadoOperacao } from '../../domain/models/resultado-operacao.model';
import { ClienteContaRepository } from '../../domain/repositories/cliente-conta.repository';
import { CLIENTES_DEMONSTRACAO } from '../data/clientes-demonstracao';

interface ContaEstado {
  clienteId: number;
  numeroConta: string;
  /** Saldo antes da primeira movimentação do mock (permite fechar com o saldo da tabela). */
  saldoBase: number;
  limiteCredito: number;
  gerente: GerenteResumo;
  dataAberturaConta: string;
  movimentacoes: Movimentacao[];
}

function deltaSaldo(m: Movimentacao): number {
  return m.sentido === 'entrada' ? m.valor : -m.valor;
}

function ordenarAsc(movs: Movimentacao[]): Movimentacao[] {
  return [...movs].sort((a, b) => a.instante.localeCompare(b.instante));
}

function ordenarDesc(movs: Movimentacao[]): Movimentacao[] {
  return [...movs].sort((a, b) => b.instante.localeCompare(a.instante));
}

function formatarMoedaExtrato(valor: number): string {
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(valor);
}

function agoraIso(): string {
  const d = new Date();
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
}

function enumerarDias(inicio: string, fim: string): string[] {
  const out: string[] = [];
  let cur = new Date(inicio + 'T12:00:00');
  const end = new Date(fim + 'T12:00:00');
  while (cur <= end) {
    const y = cur.getFullYear();
    const m = String(cur.getMonth() + 1).padStart(2, '0');
    const d = String(cur.getDate()).padStart(2, '0');
    out.push(`${y}-${m}-${d}`);
    cur.setDate(cur.getDate() + 1);
  }
  return out;
}

function normalizarNumeroConta(raw: string): string {
  const d = raw.replace(/\D/g, '');
  return d.padStart(4, '0').slice(-4);
}

@Injectable({
  providedIn: 'root',
})
export class ClienteContaMockService extends ClienteContaRepository {
  private contas = new Map<number, ContaEstado>();
  private idSeq = 1;

  constructor() {
    super();
    this.resetarParaSeedInicial();
  }

  /** Para testes ou reload — recarrega o mock das imagens. */
  resetarParaSeedInicial(): void {
    this.contas = new Map(montarContasIniciais());
    this.idSeq = 9000;
  }

  override obterResumo(clienteId: number): DashboardClienteResumo | null {
    const c = this.contas.get(clienteId);
    if (!c) {
      return null;
    }
    const cliente = CLIENTES_DEMONSTRACAO.find(x => x.id === clienteId);
    const saldo = this.saldoAtual(c);
    return {
      clienteId,
      nomeCliente: cliente?.nome ?? 'Cliente',
      emailCliente: cliente?.email ?? '',
      numeroConta: c.numeroConta,
      saldo,
      salario: cliente?.salario ?? 0,
      limiteCredito: c.limiteCredito,
      gerente: c.gerente,
      dataAberturaConta: c.dataAberturaConta,
      ultimasMovimentacoes: ordenarDesc(c.movimentacoes).slice(0, 5),
    };
  }

  override depositar(clienteId: number, valor: number): ResultadoOperacao {
    if (valor <= 0 || !Number.isFinite(valor)) {
      return { sucesso: false, mensagem: 'Informe um valor válido.' };
    }
    const c = this.contas.get(clienteId);
    if (!c) {
      return { sucesso: false, mensagem: 'Conta não encontrada.' };
    }
    const m: Movimentacao = {
      id: `op-${this.idSeq++}`,
      instante: agoraIso(),
      tipo: 'deposito',
      sentido: 'entrada',
      valor,
      contraparteLabel: '',
    };
    c.movimentacoes.push(m);
    const novo = this.saldoAtual(c);
    return {
      sucesso: true,
      mensagem: `Depósito realizado! Novo saldo: ${formatarMoedaExtrato(novo)}`,
    };
  }

  override sacar(clienteId: number, valor: number): ResultadoOperacao {
    if (valor <= 0 || !Number.isFinite(valor)) {
      return { sucesso: false, mensagem: 'Informe um valor válido.' };
    }
    const c = this.contas.get(clienteId);
    if (!c) {
      return { sucesso: false, mensagem: 'Conta não encontrada.' };
    }
    const disponivel = this.saldoAtual(c) + c.limiteCredito;
    if (valor > disponivel) {
      return {
        sucesso: false,
        mensagem: `Saldo insuficiente. Disponível: ${formatarMoedaExtrato(disponivel)}`,
      };
    }
    const m: Movimentacao = {
      id: `op-${this.idSeq++}`,
      instante: agoraIso(),
      tipo: 'saque',
      sentido: 'saida',
      valor,
      contraparteLabel: '',
    };
    c.movimentacoes.push(m);
    const novo = this.saldoAtual(c);
    return {
      sucesso: true,
      mensagem: `Saque realizado! Novo saldo: ${formatarMoedaExtrato(novo)}`,
    };
  }

  override transferir(clienteIdOrigem: number, numeroContaDestino: string, valor: number): ResultadoOperacao {
    if (valor <= 0 || !Number.isFinite(valor)) {
      return { sucesso: false, mensagem: 'Informe um valor válido.' };
    }
    const origem = this.contas.get(clienteIdOrigem);
    if (!origem) {
      return { sucesso: false, mensagem: 'Conta de origem não encontrada.' };
    }
    const destNorm = normalizarNumeroConta(numeroContaDestino);
    if (destNorm.length !== 4) {
      return { sucesso: false, mensagem: 'Número da conta deve ter 4 dígitos.' };
    }
    if (destNorm === origem.numeroConta.padStart(4, '0')) {
      return { sucesso: false, mensagem: 'Não é possível transferir para a própria conta.' };
    }
    const destino = [...this.contas.values()].find(x => x.numeroConta.padStart(4, '0') === destNorm);
    if (!destino) {
      return { sucesso: false, mensagem: 'Conta destino não encontrada.' };
    }
    const disponivel = this.saldoAtual(origem) + origem.limiteCredito;
    if (valor > disponivel) {
      return {
        sucesso: false,
        mensagem: `Saldo insuficiente. Disponível: ${formatarMoedaExtrato(disponivel)}`,
      };
    }
    const cliOrigem = CLIENTES_DEMONSTRACAO.find(c => c.id === origem.clienteId);
    const cliDest = CLIENTES_DEMONSTRACAO.find(c => c.id === destino.clienteId);
    const instante = agoraIso();
    const labelDest = `${cliDest?.nome ?? 'Cliente'} (${destino.numeroConta})`;
    const labelOrig = `${cliOrigem?.nome ?? 'Cliente'} (${origem.numeroConta})`;

    origem.movimentacoes.push({
      id: `op-${this.idSeq++}`,
      instante,
      tipo: 'transferencia',
      sentido: 'saida',
      valor,
      contraparteLabel: labelDest,
    });
    destino.movimentacoes.push({
      id: `op-${this.idSeq++}`,
      instante,
      tipo: 'transferencia',
      sentido: 'entrada',
      valor,
      contraparteLabel: labelOrig,
    });

    const [ymd] = instante.split('T');
    const [yy, mm, dd] = ymd.split('-');
    const dataFmt = `${dd}/${mm}/${yy}`;
    return {
      sucesso: true,
      mensagem: `Transferido ${formatarMoedaExtrato(valor)} para ${destino.numeroConta} | ${dataFmt}`,
    };
  }

  override consultarExtrato(clienteId: number, dataInicioIso: string, dataFimIso: string): ExtratoDia[] {
    const c = this.contas.get(clienteId);
    if (!c) {
      return [];
    }
    if (dataInicioIso > dataFimIso) {
      return [];
    }
    const movsAsc = ordenarAsc(c.movimentacoes);
    const dias = enumerarDias(dataInicioIso, dataFimIso);
    const resultado: ExtratoDia[] = [];

    for (const dia of dias) {
      const doDia = movsAsc.filter(m => m.instante.slice(0, 10) === dia);
      let saldoAntes =
        c.saldoBase +
        movsAsc
          .filter(m => m.instante.slice(0, 10) < dia)
          .reduce((acc, m) => acc + deltaSaldo(m), 0);
      for (const m of doDia) {
        saldoAntes += deltaSaldo(m);
      }
      resultado.push({
        data: dia,
        movimentacoes: doDia,
        saldoConsolidadoFimDia: saldoAntes,
      });
    }
    return resultado;
  }

  private saldoAtual(c: ContaEstado): number {
    return c.saldoBase + c.movimentacoes.reduce((acc, m) => acc + deltaSaldo(m), 0);
  }
}

function montarContasIniciais(): [number, ContaEstado][] {
  const gGenieve: GerenteResumo = { nome: 'Geniéve', email: 'genieve@bantads.com.br' };
  const gGodophredo: GerenteResumo = { nome: 'Godophredo', email: 'godophredo@bantads.com.br' };
  const gGyandula: GerenteResumo = { nome: 'Gyândula', email: 'gyandula@bantads.com.br' };

  const movCatharyna: Movimentacao[] = [
    {
      id: 'c1',
      instante: '2020-01-01T10:00:00',
      tipo: 'deposito',
      sentido: 'entrada',
      valor: 1000,
      contraparteLabel: '',
    },
    {
      id: 'c2',
      instante: '2020-01-01T11:00:00',
      tipo: 'deposito',
      sentido: 'entrada',
      valor: 900,
      contraparteLabel: '',
    },
    {
      id: 'c3',
      instante: '2020-01-01T12:00:00',
      tipo: 'saque',
      sentido: 'saida',
      valor: 550,
      contraparteLabel: '',
    },
    {
      id: 'c4',
      instante: '2020-01-01T13:00:00',
      tipo: 'saque',
      sentido: 'saida',
      valor: 350,
      contraparteLabel: '',
    },
    {
      id: 'c5',
      instante: '2020-01-10T15:00:00',
      tipo: 'deposito',
      sentido: 'entrada',
      valor: 2000,
      contraparteLabel: '',
    },
    {
      id: 'c6',
      instante: '2020-01-15T08:00:00',
      tipo: 'saque',
      sentido: 'saida',
      valor: 500,
      contraparteLabel: '',
    },
    {
      id: 'c7',
      instante: '2020-01-20T12:00:00',
      tipo: 'transferencia',
      sentido: 'saida',
      valor: 1700,
      contraparteLabel: 'Cleuddônio (0950)',
    },
  ];

  const movCleuddonio: Movimentacao[] = [
    {
      id: 'cl1',
      instante: '2025-01-01T12:00:00',
      tipo: 'deposito',
      sentido: 'entrada',
      valor: 1000,
      contraparteLabel: '',
    },
    {
      id: 'cl2',
      instante: '2025-01-02T10:00:00',
      tipo: 'deposito',
      sentido: 'entrada',
      valor: 5000,
      contraparteLabel: '',
    },
    {
      id: 'cl3',
      instante: '2025-01-10T10:00:00',
      tipo: 'saque',
      sentido: 'saida',
      valor: 200,
      contraparteLabel: '',
    },
    {
      id: 'cl4',
      instante: '2025-02-05T10:00:00',
      tipo: 'deposito',
      sentido: 'entrada',
      valor: 7000,
      contraparteLabel: '',
    },
  ];

  const movCatianna: Movimentacao[] = [
    {
      id: 'ca1',
      instante: '2025-05-05T10:00:00',
      tipo: 'deposito',
      sentido: 'entrada',
      valor: 1000,
      contraparteLabel: '',
    },
    {
      id: 'ca2',
      instante: '2025-05-06T10:00:00',
      tipo: 'saque',
      sentido: 'saida',
      valor: 2000,
      contraparteLabel: '',
    },
  ];

  const movCutardo: Movimentacao[] = [
    {
      id: 'cu1',
      instante: '2025-06-01T10:00:00',
      tipo: 'deposito',
      sentido: 'entrada',
      valor: 150000,
      contraparteLabel: '',
    },
  ];

  const movCoandrya: Movimentacao[] = [
    {
      id: 'co1',
      instante: '2025-07-01T10:00:00',
      tipo: 'deposito',
      sentido: 'entrada',
      valor: 1500,
      contraparteLabel: '',
    },
  ];

  const c1: ContaEstado = {
    clienteId: 1,
    numeroConta: '1291',
    saldoBase: 0,
    limiteCredito: 5000,
    gerente: gGenieve,
    dataAberturaConta: '2000-01-01',
    movimentacoes: [...movCatharyna],
  };

  const c2: ContaEstado = {
    clienteId: 2,
    numeroConta: '0950',
    saldoBase: -22800,
    limiteCredito: 10000,
    gerente: gGodophredo,
    dataAberturaConta: '1990-10-10',
    movimentacoes: [...movCleuddonio],
  };

  const c3: ContaEstado = {
    clienteId: 3,
    numeroConta: '8573',
    saldoBase: 0,
    limiteCredito: 1500,
    gerente: gGyandula,
    dataAberturaConta: '2012-12-12',
    movimentacoes: [...movCatianna],
  };

  const c4: ContaEstado = {
    clienteId: 4,
    numeroConta: '5887',
    saldoBase: 0,
    limiteCredito: 0,
    gerente: gGenieve,
    dataAberturaConta: '2022-02-22',
    movimentacoes: [...movCutardo],
  };

  const c5: ContaEstado = {
    clienteId: 5,
    numeroConta: '7617',
    saldoBase: 0,
    limiteCredito: 0,
    gerente: gGodophredo,
    dataAberturaConta: '2025-01-01',
    movimentacoes: [...movCoandrya],
  };

  return [
    [1, c1],
    [2, c2],
    [3, c3],
    [4, c4],
    [5, c5],
  ];
}
