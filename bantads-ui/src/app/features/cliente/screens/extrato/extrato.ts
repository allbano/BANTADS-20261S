import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { SessaoClienteService } from '../../../../core/auth/services/sessao-cliente.service';
import { ExtratoFacade } from '../../application/facades/extrato.facade';
import { ClienteTopNav } from '../../components/cliente-top-nav/cliente-top-nav';
import type { Movimentacao } from '../../domain/models/movimentacao.model';
import type { TipoMovimentacao } from '../../domain/models/tipo-movimentacao';

@Component({
  selector: 'app-extrato',
  imports: [FormsModule, CurrencyPipe, DatePipe, DecimalPipe, ClienteTopNav],
  templateUrl: './extrato.html',
  providers: [ExtratoFacade],
})
export class Extrato implements OnInit {
  readonly facade = inject(ExtratoFacade);
  private readonly sessao = inject(SessaoClienteService);

  protected dataInicio = '2020-01-01';
  protected dataFim = '2020-01-31';

  ngOnInit(): void {
    const id = this.sessao.clienteId();
    if (id === 2) {
      this.dataInicio = '2025-01-01';
      this.dataFim = '2025-12-31';
    } else if (id === 3) {
      this.dataInicio = '2025-05-01';
      this.dataFim = '2025-05-31';
    } else if (id === 4) {
      this.dataInicio = '2025-06-01';
      this.dataFim = '2025-06-30';
    } else if (id === 5) {
      this.dataInicio = '2025-07-01';
      this.dataFim = '2025-07-31';
    }
    this.facade.atualizarResumo();
    this.facade.aplicarFiltro(this.dataInicio, this.dataFim);
  }

  protected filtrar(): void {
    this.facade.aplicarFiltro(this.dataInicio, this.dataFim);
  }

  protected formatarDia(iso: string): string {
    const [y, m, d] = iso.split('-');
    return `${d}/${m}/${y}`;
  }

  protected rotuloTipo(tipo: TipoMovimentacao): string {
    switch (tipo) {
      case 'deposito':
        return 'Depósito';
      case 'saque':
        return 'Saque';
      case 'transferencia':
        return 'Transferência';
      default:
        return tipo;
    }
  }

  protected classeLinha(m: Movimentacao): string {
    return m.sentido === 'entrada' ? '[&_td]:text-blue-400' : '[&_td]:text-red-400';
  }

  protected prefixoValor(m: Movimentacao): string {
    return m.sentido === 'entrada' ? '+ R$ ' : '- R$ ';
  }

  protected contraparte(m: Movimentacao): string {
    return m.contraparteLabel.trim() || '—';
  }

  protected saldoNegativo(saldo: number): boolean {
    return saldo < 0;
  }
}
