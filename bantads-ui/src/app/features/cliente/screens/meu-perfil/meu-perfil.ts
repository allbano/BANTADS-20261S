import { Component, OnInit, inject } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { CurrencyPipe } from '@angular/common';

import { PerfilClienteFacade } from '../../application/facades/perfil-cliente.facade';
import { ClienteTopNav } from '../../components/cliente-top-nav/cliente-top-nav';
import { DashboardClienteRepository } from '../../domain/repositories/dashboard-cliente.repository';
import { PerfilClienteRepository } from '../../domain/repositories/perfil-cliente.repository';
import { ClienteContaMockService } from '../../infrastructure/services/cliente-conta-mock.service';
import { PerfilClienteMockService } from '../../infrastructure/services/perfil-cliente-mock.service';

@Component({
  selector: 'app-meu-perfil',
  imports: [ReactiveFormsModule, CurrencyPipe, ClienteTopNav],
  templateUrl: './meu-perfil.html',
  providers: [
    PerfilClienteFacade,
    { provide: PerfilClienteRepository, useExisting: PerfilClienteMockService },
    { provide: DashboardClienteRepository, useExisting: ClienteContaMockService },
  ],
})
export class MeuPerfil implements OnInit {
  readonly facade = inject(PerfilClienteFacade);

  /** Controla se o painel de alterar senha está expandido. */
  protected senhaAberta = false;

  ngOnInit(): void {
    this.facade.carregar();
  }

  protected toggleSenha(): void {
    this.senhaAberta = !this.senhaAberta;
    if (!this.senhaAberta) {
      this.facade.formSenha.reset();
      this.facade.limparFeedbacks();
    }
  }
}
