import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import type { Movimentacao } from '../../domain/models/movimentacao.model';
import type { TipoMovimentacao } from '../../domain/models/tipo-movimentacao';

@Component({
  selector: 'app-ultimas-movimentacoes',
  imports: [DatePipe, DecimalPipe, RouterLink],
  templateUrl: './ultimas-movimentacoes.html',
})
export class UltimasMovimentacoes {
  readonly movimentacoes = input<Movimentacao[]>([]);
  readonly limiteExibicao = input<number>(5);

  protected linhas(): Movimentacao[] {
    return this.movimentacoes().slice(0, this.limiteExibicao());
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

  protected prefixoValor(m: Movimentacao): string {
    return m.sentido === 'entrada' ? '+ R$ ' : '- R$ ';
  }

  /** R8: cor da linha inteira via utilitários Tailwind nos <td> filhos. */
  protected classeLinha(m: Movimentacao): string {
    return m.sentido === 'entrada' ? '[&_td]:text-blue-400' : '[&_td]:text-red-400';
  }

  protected contraparte(m: Movimentacao): string {
    return m.contraparteLabel.trim() || '—';
  }
}
