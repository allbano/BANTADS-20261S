import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';

import { AutenticacaoService } from '../../../../core/auth/services/autenticacao.service';

@Component({
  selector: 'app-cliente-top-nav',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './cliente-top-nav.html',
})
export class ClienteTopNav {
  private readonly router = inject(Router);
  private readonly autenticacao = inject(AutenticacaoService);

  sair(): void {
    this.autenticacao.logout().subscribe(() => {
      void this.router.navigateByUrl('/auth/login');
    });
  }
}
