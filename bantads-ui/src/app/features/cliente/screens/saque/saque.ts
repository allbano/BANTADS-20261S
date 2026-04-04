import { CurrencyPipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { SaqueFacade } from '../../application/facades/saque.facade';
import { ClienteTopNav } from '../../components/cliente-top-nav/cliente-top-nav';
import { ResumoContaLateral } from '../../components/resumo-conta-lateral/resumo-conta-lateral';

@Component({
  selector: 'app-saque',
  imports: [FormsModule, CurrencyPipe, ClienteTopNav, ResumoContaLateral],
  templateUrl: './saque.html',
  providers: [SaqueFacade],
})
export class Saque implements OnInit {
  readonly facade = inject(SaqueFacade);

  protected valor = 0;

  ngOnInit(): void {
    this.facade.recarregar();
  }

  protected disponivel(r: { saldo: number; limiteCredito: number }): number {
    return r.saldo + r.limiteCredito;
  }

  protected definirRapido(v: number): void {
    this.valor = v;
  }

  protected confirmar(): void {
    this.facade.sacar(this.valor);
  }
}
