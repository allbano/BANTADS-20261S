import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { SessaoClienteService } from '../../services/sessao-cliente.service';
import { SessaoGerenteService } from '../../services/sessao-gerente.service';
import { SessaoAdminService } from '../../services/sessao-admin.service';
import { ClienteAutenticacaoService } from '../../../../features/cliente/infrastructure/services/cliente-autenticacao.service';
import { ADMINISTRADOR_MOCK } from '../../../../features/administrador/infrastructure/data/administrador-mock';
import { GERENTES_MOCK } from '../../../../features/administrador/infrastructure/data/gerentes-mock';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './login.html',
})
export class Login {
  private readonly autenticacaoCliente = inject(ClienteAutenticacaoService);
  private readonly sessaoCliente = inject(SessaoClienteService);
  private readonly sessaoGerente = inject(SessaoGerenteService);
  private readonly sessaoAdmin = inject(SessaoAdminService);
  private readonly router = inject(Router);

  protected email = '';
  protected senha = '';
  protected readonly erro = signal<string | null>(null);

  protected onSubmit(event: Event): void {
    event.preventDefault();
    this.erro.set(null);

    const emailNorm = this.email.trim().toLowerCase();

    // 1. Verifica se é administrador
    if (
      ADMINISTRADOR_MOCK.email.toLowerCase() === emailNorm &&
      ADMINISTRADOR_MOCK.senha === this.senha
    ) {
      this.sessaoAdmin.iniciar(ADMINISTRADOR_MOCK.id);
      void this.router.navigateByUrl('/admin/dashboard');
      return;
    }

    // 2. Verifica se é gerente
    const gerente = GERENTES_MOCK.find(
      g => g.email.toLowerCase() === emailNorm && g.senha === this.senha
    );
    if (gerente) {
      this.sessaoGerente.iniciar(gerente.id);
      void this.router.navigateByUrl('/gerente/dashboard');
      return;
    }

    // 3. Verifica se é cliente
    const cliente = this.autenticacaoCliente.buscarPorCredencial(this.email, this.senha);
    if (cliente) {
      this.sessaoCliente.iniciar(cliente.id);
      void this.router.navigateByUrl('/cliente/dashboard');
      return;
    }

    this.erro.set('E-mail ou senha inválidos.');
  }
}
