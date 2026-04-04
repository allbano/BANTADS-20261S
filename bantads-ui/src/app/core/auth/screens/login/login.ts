import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { SessaoClienteService } from '../../services/sessao-cliente.service';
import { ClienteAutenticacaoService } from '../../../../features/cliente/infrastructure/services/cliente-autenticacao.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './login.html',
})
export class Login {
  private readonly autenticacao = inject(ClienteAutenticacaoService);
  private readonly sessao = inject(SessaoClienteService);
  private readonly router = inject(Router);

  protected email = '';
  protected senha = '';
  protected readonly erro = signal<string | null>(null);

  protected onSubmit(event: Event): void {
    event.preventDefault();
    this.erro.set(null);
    const cliente = this.autenticacao.buscarPorCredencial(this.email, this.senha);
    if (!cliente) {
      this.erro.set('E-mail ou senha inválidos.');
      return;
    }
    this.sessao.iniciar(cliente.id);
    void this.router.navigateByUrl('/cliente/dashboard');
  }
}
