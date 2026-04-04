import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { CrudGerentesFacade } from '../../application/facades/crud-gerentes.facade';
import { GerenteAdminRepository } from '../../domain/repositories/gerente-admin.repository';
import { GerenteAdminMockService } from '../../infrastructure/services/gerente-admin-mock.service';
import { AdminTopNav } from '../../components/admin-top-nav/admin-top-nav';
import type { Gerente } from '../../domain/models/gerente.model';

/**
 * R17–R20 — CRUD de Gerentes.
 *
 * R17: Inserção com redistribuição de contas.
 * R18: Remoção com transferência de contas (último gerente bloqueado).
 * R19: Listagem ordenada por nome crescente.
 * R20: Alteração apenas de nome, e-mail e senha.
 */
@Component({
  selector: 'app-crud-gerentes',
  imports: [FormsModule, AdminTopNav],
  templateUrl: './crud-gerentes.html',
  providers: [
    CrudGerentesFacade,
    { provide: GerenteAdminRepository, useExisting: GerenteAdminMockService },
  ],
})
export class CrudGerentesComponent implements OnInit {
  readonly facade = inject(CrudGerentesFacade);

  // Controle do Modal
  readonly exibirModal = signal(false);
  readonly modoModal = signal<'inserir' | 'editar'>('inserir');

  gerenteForm: Partial<Gerente> = {};

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
        nome: this.gerenteForm.nome ?? '',
        cpf: this.gerenteForm.cpf ?? '',
        email: this.gerenteForm.email ?? '',
        telefone: this.gerenteForm.telefone ?? '',
        senha: this.gerenteForm.senha ?? '',
        tipo: 'gerente',
      });
    } else {
      this.facade.atualizar(this.gerenteForm.id!, {
        nome: this.gerenteForm.nome ?? '',
        email: this.gerenteForm.email ?? '',
        senha: this.gerenteForm.senha || undefined,
      });
    }

    this.fecharModal();
  }

  excluirGerente(id: number): void {
    if (confirm('Tem certeza que deseja excluir este gerente?')) {
      const resultado = this.facade.remover(id);
      if (!resultado.sucesso) {
        alert(resultado.mensagem);
      }
    }
  }
}