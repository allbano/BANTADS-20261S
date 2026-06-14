import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { ExtratoFacade } from '../../application/facades/extrato.facade';
import { ClienteTopNav } from '../../components/cliente-top-nav/cliente-top-nav';
import type { Movimentacao } from '../../domain/models/movimentacao.model';
import type { TipoMovimentacao } from '../../domain/models/tipo-movimentacao';
import { ClienteContaRepository } from '../../domain/repositories/cliente-conta.repository';
import { ClienteContaHttpService } from '../../infrastructure/services/cliente-conta-http.service';

function isoHoje(): string {
  return new Date().toISOString().slice(0, 10);
}

@Component({
  selector: 'app-extrato',
  imports: [FormsModule, CurrencyPipe, DatePipe, DecimalPipe, ClienteTopNav],
  templateUrl: './extrato.html',
  providers: [
    ExtratoFacade,
    { provide: ClienteContaRepository, useExisting: ClienteContaHttpService },
  ],
})
export class Extrato implements OnInit {
  readonly facade = inject(ExtratoFacade);

  // Período padrão amplo, cobrindo o histórico completo da conta.
  protected dataInicio = '2000-01-01';
  protected dataFim = isoHoje();

  ngOnInit(): void {
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
