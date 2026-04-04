import { Component } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-gerente-top-nav',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './gerente-top-nav.html',
})
export class GerenteTopNav {
  constructor(private readonly router: Router) {}

  sair(): void {
    void this.router.navigateByUrl('/auth/login');
  }
}
