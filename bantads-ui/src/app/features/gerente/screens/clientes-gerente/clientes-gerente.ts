import { Component, inject, OnInit } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { ClientesGerenteFacade } from '../../application/facades/clientes-gerente.facade';
import { ClientesGerenteRepository } from '../../domain/repositories/clientes-gerente.repository';
import { ClientesGerenteMockService } from '../../infrastructure/services/clientes-gerente-mock.service';
import { GerenteTopNav } from '../../components/gerente-top-nav/gerente-top-nav';

/**
 * R12 — Consultar Todos os Clientes.
 * Tabela com CPF, Nome, Cidade, Estado, Saldo, Limite.
 * Ordenado por nome, com pesquisa por CPF/Nome.
 */
@Component({
  selector: 'app-clientes-gerente',
  imports: [CurrencyPipe, RouterLink, FormsModule, GerenteTopNav],
  templateUrl: './clientes-gerente.html',
  providers: [
    ClientesGerenteFacade,
    { provide: ClientesGerenteRepository, useExisting: ClientesGerenteMockService },
  ],
})
export class ClientesGerente implements OnInit {
  readonly facade = inject(ClientesGerenteFacade);

  ngOnInit(): void {
    this.facade.carregar();
  }
}
