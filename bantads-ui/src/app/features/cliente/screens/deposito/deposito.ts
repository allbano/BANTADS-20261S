import { CurrencyPipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { DepositoFacade } from '../../application/facades/deposito.facade';
import { ClienteTopNav } from '../../components/cliente-top-nav/cliente-top-nav';
import { ResumoContaLateral } from '../../components/resumo-conta-lateral/resumo-conta-lateral';
import { ClienteContaRepository } from '../../domain/repositories/cliente-conta.repository';
import { ClienteContaHttpService } from '../../infrastructure/services/cliente-conta-http.service';

@Component({
  selector: 'app-deposito',
  imports: [FormsModule, CurrencyPipe, ClienteTopNav, ResumoContaLateral],
  templateUrl: './deposito.html',
  providers: [
    DepositoFacade,
    { provide: ClienteContaRepository, useExisting: ClienteContaHttpService },
  ],
})
export class Deposito implements OnInit {
  readonly facade = inject(DepositoFacade);

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
    this.facade.depositar(this.valor);
  }
}
