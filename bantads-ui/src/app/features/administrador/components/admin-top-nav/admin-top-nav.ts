import { Component } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-admin-top-nav',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './admin-top-nav.html',
})
export class AdminTopNav {
  constructor(private readonly router: Router) {}

  sair(): void {
    void this.router.navigateByUrl('/auth/login');
  }
}
