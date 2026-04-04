import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';

import { DashboardClienteFacade } from '../../application/facades/dashboard-cliente.facade';
import { ClienteTopNav } from '../../components/cliente-top-nav/cliente-top-nav';
import { UltimasMovimentacoes } from '../../components/ultimas-movimentacoes/ultimas-movimentacoes';
import { DashboardClienteRepository } from '../../domain/repositories/dashboard-cliente.repository';
import { ClienteContaMockService } from '../../infrastructure/services/cliente-conta-mock.service';

@Component({
  selector: 'app-dashboard',
  imports: [
    RouterLink,
    CurrencyPipe,
    DatePipe,
    ClienteTopNav,
    UltimasMovimentacoes,
  ],
  templateUrl: './dashboard.html',
  providers: [
    DashboardClienteFacade,
    { provide: DashboardClienteRepository, useExisting: ClienteContaMockService },
  ],
})
export class Dashboard implements OnInit {
  readonly facade = inject(DashboardClienteFacade);

  ngOnInit(): void {
    this.facade.carregar();
  }

  protected primeiroNome(nomeCompleto: string): string {
    return nomeCompleto.trim().split(/\s+/)[0] ?? nomeCompleto;
  }
}
