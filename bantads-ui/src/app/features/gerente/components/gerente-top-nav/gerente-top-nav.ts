import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';

import { AutenticacaoService } from '../../../../core/auth/services/autenticacao.service';

@Component({
  selector: 'app-gerente-top-nav',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './gerente-top-nav.html',
})
export class GerenteTopNav {
  private readonly router = inject(Router);
  private readonly autenticacao = inject(AutenticacaoService);

  sair(): void {
    this.autenticacao.logout().subscribe(() => {
      void this.router.navigateByUrl('/auth/login');
    });
  }
}
