import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CurrencyPipe, DatePipe } from '@angular/common';

import { ClientesGerenteFacade } from '../../application/facades/clientes-gerente.facade';
import { ClientesGerenteRepository } from '../../domain/repositories/clientes-gerente.repository';
import { ClientesGerenteHttpService } from '../../infrastructure/services/clientes-gerente-http.service';
import { GerenteTopNav } from '../../components/gerente-top-nav/gerente-top-nav';

/**
 * R13 — Tela de detalhe completo do cliente + conta (acessada por CPF).
 */
@Component({
  selector: 'app-detalhe-cliente',
  imports: [CurrencyPipe, DatePipe, RouterLink, GerenteTopNav],
  templateUrl: './detalhe-cliente.html',
  providers: [
    ClientesGerenteFacade,
    { provide: ClientesGerenteRepository, useExisting: ClientesGerenteHttpService },
  ],
})
export class DetalheCliente implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly facade = inject(ClientesGerenteFacade);

  /** Cliente selecionado, carregado da API por CPF. */
  readonly cliente = this.facade.selecionado;

  ngOnInit(): void {
    const cpf = this.route.snapshot.paramMap.get('id');
    if (cpf) {
      this.facade.carregarPorCpf(cpf);
    }
  }
}
