import { Component, inject, OnInit } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { RelatorioAdminFacade } from '../../application/facades/relatorio-admin.facade';
import { RelatorioAdminRepository } from '../../domain/repositories/relatorio-admin.repository';
import { RelatorioAdminMockService } from '../../infrastructure/services/relatorio-admin-mock.service';
import { AdminTopNav } from '../../components/admin-top-nav/admin-top-nav';

/**
 * R16 — Relatório de Clientes.
 *
 * Lista todos os clientes com: CPF, Nome, E-mail, Salário,
 * Número da conta, Saldo, Limite, CPF do gerente e Nome do gerente.
 * Ordenação crescente por nome do cliente.
 */
@Component({
  selector: 'app-relatorio',
  imports: [DecimalPipe, FormsModule, AdminTopNav],
  templateUrl: './relatorio.html',
  providers: [
    RelatorioAdminFacade,
    { provide: RelatorioAdminRepository, useExisting: RelatorioAdminMockService },
  ],
})
export class RelatorioComponent implements OnInit {
  readonly facade = inject(RelatorioAdminFacade);

  ngOnInit(): void {
    this.facade.carregar();
  }
}