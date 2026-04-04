import { Component, inject, OnInit } from '@angular/core';
import { CurrencyPipe } from '@angular/common';

import { TopClientesFacade } from '../../application/facades/top-clientes.facade';
import { TopClientesRepository } from '../../domain/repositories/top-clientes.repository';
import { TopClientesMockService } from '../../infrastructure/services/top-clientes-mock.service';
import { GerenteTopNav } from '../../components/gerente-top-nav/gerente-top-nav';

/**
 * R14 — Top 3 maiores saldos (qualquer gerente).
 */
@Component({
  selector: 'app-top-clientes',
  imports: [CurrencyPipe, GerenteTopNav],
  templateUrl: './top-clientes.html',
  providers: [
    TopClientesFacade,
    { provide: TopClientesRepository, useExisting: TopClientesMockService },
  ],
})
export class TopClientes implements OnInit {
  readonly facade = inject(TopClientesFacade);

  ngOnInit(): void {
    this.facade.carregar();
  }
}
