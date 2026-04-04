import { CurrencyPipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { TransferenciaFacade } from '../../application/facades/transferencia.facade';
import { ClienteTopNav } from '../../components/cliente-top-nav/cliente-top-nav';
import { ResumoContaLateral } from '../../components/resumo-conta-lateral/resumo-conta-lateral';

@Component({
  selector: 'app-transferencia',
  imports: [FormsModule, CurrencyPipe, ClienteTopNav, ResumoContaLateral],
  templateUrl: './transferencia.html',
  providers: [TransferenciaFacade],
})
export class Transferencia implements OnInit {
  readonly facade = inject(TransferenciaFacade);

  protected valor = 0;
  protected contaDestino = '';

  ngOnInit(): void {
    this.facade.recarregar();
  }

  protected disponivel(r: { saldo: number; limiteCredito: number }): number {
    return r.saldo + r.limiteCredito;
  }

  protected confirmar(): void {
    this.facade.transferir(this.contaDestino.trim(), this.valor);
  }
}
