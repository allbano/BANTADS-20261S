import { Component, inject, OnInit } from '@angular/core';
import { DecimalPipe } from '@angular/common';

import { DashboardAdminFacade } from '../../application/facades/dashboard-admin.facade';
import { DashboardAdminRepository } from '../../domain/repositories/dashboard-admin.repository';
import { DashboardAdminHttpService } from '../../infrastructure/services/dashboard-admin-http.service';
import { AdminTopNav } from '../../components/admin-top-nav/admin-top-nav';

/**
 * R15 — Dashboard do Administrador.
 *
 * Apresenta todos os gerentes com: quantos clientes possui,
 * soma de saldos positivos e soma de saldos negativos.
 * Ordenado por maior saldo positivo primeiro.
 */
@Component({
  selector: 'app-dashboard-admin',
  imports: [DecimalPipe, AdminTopNav],
  templateUrl: './dashbord-admin.html',
  providers: [
    DashboardAdminFacade,
    { provide: DashboardAdminRepository, useExisting: DashboardAdminHttpService },
  ],
})
export class DashboardAdminComponent implements OnInit {
  readonly facade = inject(DashboardAdminFacade);

  ngOnInit(): void {
    this.facade.carregar();
  }
}