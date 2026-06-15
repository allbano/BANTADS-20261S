import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';

import { AutenticacaoService } from '../../../../core/auth/services/autenticacao.service';

@Component({
  selector: 'app-admin-top-nav',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './admin-top-nav.html',
})
export class AdminTopNav {
  private readonly router = inject(Router);
  private readonly autenticacao = inject(AutenticacaoService);

  sair(): void {
    this.autenticacao.logout().subscribe(() => {
      void this.router.navigateByUrl('/auth/login');
    });
  }
}
