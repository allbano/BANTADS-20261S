import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';

import { SessaoClienteService } from '../../../../core/auth/services/sessao-cliente.service';

@Component({
  selector: 'app-cliente-top-nav',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './cliente-top-nav.html',
})
export class ClienteTopNav {
  private readonly router = inject(Router);
  private readonly sessao = inject(SessaoClienteService);

  sair(): void {
    this.sessao.encerrar();
    void this.router.navigateByUrl('/auth/login');
  }
}
