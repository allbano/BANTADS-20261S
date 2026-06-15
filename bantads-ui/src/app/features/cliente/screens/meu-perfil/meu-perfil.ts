import { Component, OnInit, inject } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { CurrencyPipe } from '@angular/common';

import { PerfilClienteFacade } from '../../application/facades/perfil-cliente.facade';
import { ClienteTopNav } from '../../components/cliente-top-nav/cliente-top-nav';
import { DashboardClienteRepository } from '../../domain/repositories/dashboard-cliente.repository';
import { PerfilClienteRepository } from '../../domain/repositories/perfil-cliente.repository';
import { ClienteContaHttpService } from '../../infrastructure/services/cliente-conta-http.service';
import { PerfilClienteHttpService } from '../../infrastructure/services/perfil-cliente-http.service';

@Component({
  selector: 'app-meu-perfil',
  imports: [ReactiveFormsModule, CurrencyPipe, ClienteTopNav],
  templateUrl: './meu-perfil.html',
  providers: [
    PerfilClienteFacade,
    { provide: PerfilClienteRepository, useExisting: PerfilClienteHttpService },
    { provide: DashboardClienteRepository, useExisting: ClienteContaHttpService },
  ],
})
export class MeuPerfil implements OnInit {
  readonly facade = inject(PerfilClienteFacade);

  ngOnInit(): void {
    this.facade.carregar();
  }
}
