import { CurrencyPipe } from '@angular/common';
import { Component, input } from '@angular/core';

@Component({
  selector: 'app-resumo-conta-lateral',
  imports: [CurrencyPipe],
  templateUrl: './resumo-conta-lateral.html',
})
export class ResumoContaLateral {
  readonly numeroConta = input.required<string>();
  readonly saldo = input.required<number>();
  readonly limite = input.required<number>();
  readonly disponivel = input.required<number>();

  protected saldoNegativo(): boolean {
    return this.saldo() < 0;
  }
}
