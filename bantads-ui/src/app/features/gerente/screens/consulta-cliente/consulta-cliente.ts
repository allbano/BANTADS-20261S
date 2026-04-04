import { Component, inject } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { ConsultaClienteFacade } from '../../application/facades/consulta-cliente.facade';
import { ClientesGerenteRepository } from '../../domain/repositories/clientes-gerente.repository';
import { ClientesGerenteMockService } from '../../infrastructure/services/clientes-gerente-mock.service';
import { GerenteTopNav } from '../../components/gerente-top-nav/gerente-top-nav';

/**
 * R13 — Consultar Cliente por CPF.
 */
@Component({
  selector: 'app-consulta-cliente',
  imports: [CurrencyPipe, FormsModule, GerenteTopNav],
  templateUrl: './consulta-cliente.html',
  providers: [
    ConsultaClienteFacade,
    { provide: ClientesGerenteRepository, useExisting: ClientesGerenteMockService },
  ],
})
export class ConsultaCliente {
  readonly facade = inject(ConsultaClienteFacade);
  cpfInput = '';

  pesquisar(): void {
    this.facade.pesquisarPorCpf(this.cpfInput);
  }
}
