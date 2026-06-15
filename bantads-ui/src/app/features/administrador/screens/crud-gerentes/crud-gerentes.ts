import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { CrudGerentesFacade } from '../../application/facades/crud-gerentes.facade';
import { GerenteAdminRepository } from '../../domain/repositories/gerente-admin.repository';
import { GerenteAdminHttpService } from '../../infrastructure/services/gerente-admin-http.service';
import { AdminTopNav } from '../../components/admin-top-nav/admin-top-nav';
import type { Gerente } from '../../domain/models/gerente.model';

/**
 * R17–R20 — CRUD de Gerentes.
 *
 * R17: Inserção (redistribuição de contas é feita pelo backend).
 * R18: Remoção (último gerente bloqueado pelo backend).
 * R19: Listagem ordenada por nome crescente.
 * R20: Alteração apenas de nome, e-mail e senha.
 */
@Component({
  selector: 'app-crud-gerentes',
  imports: [FormsModule, AdminTopNav],
  templateUrl: './crud-gerentes.html',
  providers: [
    CrudGerentesFacade,
    { provide: GerenteAdminRepository, useExisting: GerenteAdminHttpService },
  ],
})
export class CrudGerentesComponent implements OnInit {
  readonly facade = inject(CrudGerentesFacade);

  // Controle do Modal
  readonly exibirModal = signal(false);
  readonly modoModal = signal<'inserir' | 'editar'>('inserir');

  gerenteForm: Partial<Gerente> & { senha?: string } = {};

  ngOnInit(): void {
    this.facade.carregar();
  }

  abrirModalNovo(): void {
    this.modoModal.set('inserir');
    this.gerenteForm = {};
    this.exibirModal.set(true);
  }

  abrirModalEdicao(gerente: Gerente): void {
    this.modoModal.set('editar');
    this.gerenteForm = { ...gerente, senha: '' };
    this.exibirModal.set(true);
  }

  fecharModal(): void {
    this.exibirModal.set(false);
    this.gerenteForm = {};
  }

  salvarGerente(): void {
    if (this.modoModal() === 'inserir') {
      this.facade.inserir({
        cpf: (this.gerenteForm.cpf ?? '').replace(/\D/g, ''),
        nome: this.gerenteForm.nome ?? '',
        email: this.gerenteForm.email ?? '',
        telefone: this.gerenteForm.telefone ?? '',
        tipo: 'GERENTE',
        senha: this.gerenteForm.senha ?? '',
      });
    } else {
      this.facade.atualizar(this.gerenteForm.cpf ?? '', {
        nome: this.gerenteForm.nome ?? '',
        email: this.gerenteForm.email ?? '',
        senha: this.gerenteForm.senha || undefined,
      });
    }

    this.fecharModal();
  }

  excluirGerente(cpf: string): void {
    if (confirm('Tem certeza que deseja excluir este gerente?')) {
      this.facade.remover(cpf);
    }
  }
}
