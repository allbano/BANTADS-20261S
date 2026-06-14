import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';

import { AutenticacaoService } from '../../services/autenticacao.service';
import { TipoUsuario } from '../../services/sessao.service';
import { mensagemDeErro } from '../../../http/error.interceptor';

const ROTA_POR_TIPO: Record<TipoUsuario, string> = {
  CLIENTE: '/cliente/dashboard',
  GERENTE: '/gerente/dashboard',
  ADMIN: '/admin/dashboard',
};

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './login.html',
})
export class Login {
  private readonly autenticacao = inject(AutenticacaoService);
  private readonly router = inject(Router);

  protected email = '';
  protected senha = '';
  protected readonly erro = signal<string | null>(null);
  protected readonly carregando = signal(false);

  protected onSubmit(event: Event): void {
    event.preventDefault();
    this.erro.set(null);
    this.carregando.set(true);

    this.autenticacao.login(this.email.trim(), this.senha).subscribe({
      next: (sessao) => {
        this.carregando.set(false);
        void this.router.navigateByUrl(ROTA_POR_TIPO[sessao.tipo] ?? '/');
      },
      error: (err: HttpErrorResponse) => {
        this.carregando.set(false);
        this.erro.set(
          err.status === 401
            ? 'E-mail ou senha inválidos.'
            : mensagemDeErro(err, 'Não foi possível efetuar o login. Tente novamente.'),
        );
      },
    });
  }
}
