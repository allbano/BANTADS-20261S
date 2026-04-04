import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { ClienteTopNav } from '../../components/cliente-top-nav/cliente-top-nav';

@Component({
  selector: 'app-cliente-area-placeholder',
  imports: [RouterLink, ClienteTopNav],
  templateUrl: './cliente-area-placeholder.html',
})
export class ClienteAreaPlaceholder implements OnInit {
  private readonly route = inject(ActivatedRoute);

  readonly titulo = signal('Área do cliente');

  ngOnInit(): void {
    const title = this.route.snapshot.data['title'] as string | undefined;
    if (title) {
      this.titulo.set(title);
    }
  }
}
