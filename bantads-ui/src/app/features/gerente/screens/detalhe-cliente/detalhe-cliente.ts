import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CurrencyPipe, DatePipe } from '@angular/common';

import { ClientesGerenteFacade } from '../../application/facades/clientes-gerente.facade';
import { ClientesGerenteRepository } from '../../domain/repositories/clientes-gerente.repository';
import { ClientesGerenteMockService } from '../../infrastructure/services/clientes-gerente-mock.service';
import { GerenteTopNav } from '../../components/gerente-top-nav/gerente-top-nav';
import type { ClienteGerente } from '../../domain/models/cliente-gerente.model';

/**
 * R12 (link) — Tela de detalhe completo do cliente + conta.
 */
@Component({
  selector: 'app-detalhe-cliente',
  imports: [CurrencyPipe, DatePipe, RouterLink, GerenteTopNav],
  templateUrl: './detalhe-cliente.html',
  providers: [
    ClientesGerenteFacade,
    { provide: ClientesGerenteRepository, useExisting: ClientesGerenteMockService },
  ],
})
export class DetalheCliente implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly facade = inject(ClientesGerenteFacade);

  readonly cliente = signal<ClienteGerente | null>(null);

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.cliente.set(this.facade.buscarPorId(id));
    }
  }
}
