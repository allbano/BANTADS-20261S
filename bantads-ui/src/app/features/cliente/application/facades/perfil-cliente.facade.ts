import { Injectable, computed, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

import type { PerfilCliente } from '../../domain/models/perfil-cliente.model';
import { PerfilClienteRepository } from '../../domain/repositories/perfil-cliente.repository';
import { SessaoClienteService } from '../../../../core/auth/services/sessao-cliente.service';

export type FeedbackPerfil = { texto: string; erro: boolean };

/**
 * Façade da tela "Meu Perfil".
 *
 * Responsabilidades:
 *  - Carregar o perfil do cliente autenticado a partir do repositório
 *  - Expor Signals reativos para o componente consumir
 *  - Orquestrar casos de uso: salvarPerfil() e alterarSenha()
 *
 * Provido em nível de componente (providers: [...]) para que cada
 * instância da screen tenha seu próprio ciclo de vida.
 */
@Injectable()
export class PerfilClienteFacade {
  private readonly repo = inject(PerfilClienteRepository);
  private readonly sessao = inject(SessaoClienteService);
  private readonly fb = inject(FormBuilder);

  // ── Estado reativo ──────────────────────────────────────────────
  private readonly _perfil = signal<PerfilCliente | null>(null);
  private readonly _feedbackPerfil = signal<FeedbackPerfil | null>(null);
  private readonly _feedbackSenha = signal<FeedbackPerfil | null>(null);
  private readonly _carregando = signal(false);

  readonly perfil = this._perfil.asReadonly();
  readonly feedbackPerfil = this._feedbackPerfil.asReadonly();
  readonly feedbackSenha = this._feedbackSenha.asReadonly();
  readonly carregando = this._carregando.asReadonly();

  readonly nomeExibido = computed(() => {
    const p = this._perfil();
    return p ? p.nome.trim().split(/\s+/)[0] : '';
  });

  // ── Formulários reativos ────────────────────────────────────────
  readonly formDados: FormGroup = this.fb.group({
    nome: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    telefone: ['', Validators.required],
    // readonly — apenas para exibição
    cpf: [{ value: '', disabled: true }],
    salario: ['', [Validators.required, Validators.min(0)]],
  });

  readonly formEndereco: FormGroup = this.fb.group({
    cep: ['', Validators.required],
    logradouro: ['', Validators.required],
    numero: ['', Validators.required],
    complemento: [''],
    cidade: ['', Validators.required],
    uf: ['', [Validators.required, Validators.maxLength(2)]],
  });

  readonly formSenha: FormGroup = this.fb.group({
    senhaAtual: ['', Validators.required],
    novaSenha: ['', [Validators.required, Validators.minLength(4)]],
    confirmarSenha: ['', Validators.required],
  });

  // ── Casos de uso ────────────────────────────────────────────────

  carregar(): void {
    const id = this.sessao.clienteId();
    if (id === null) {
      return;
    }

    this._carregando.set(true);
    const perfil = this.repo.buscarPerfil(id);
    this._perfil.set(perfil);
    this._carregando.set(false);

    if (perfil) {
      this.formDados.patchValue({
        nome: perfil.nome,
        email: perfil.email,
        telefone: perfil.telefone,
        cpf: this.formatarCpf(perfil.cpf),
        salario: perfil.salario,
      });
      this.formEndereco.patchValue({ ...perfil.endereco });
    }
  }

  salvarPerfil(): void {
    if (this.formDados.invalid || this.formEndereco.invalid) {
      this.formDados.markAllAsTouched();
      this.formEndereco.markAllAsTouched();
      return;
    }

    const id = this.sessao.clienteId();
    if (id === null) {
      return;
    }

    const payload: PerfilCliente = {
      clienteId: id,
      nome: this.formDados.getRawValue().nome,
      cpf: this._perfil()?.cpf ?? 0,
      email: this.formDados.getRawValue().email,
      salario: Number(this.formDados.getRawValue().salario) || 0,
      telefone: this.formDados.getRawValue().telefone,
      endereco: this.formEndereco.getRawValue(),
    };

    const resultado = this.repo.salvarPerfil(payload);
    this._feedbackPerfil.set({ texto: resultado.mensagem, erro: !resultado.sucesso });

    if (resultado.sucesso) {
      this.carregar(); // re-sincroniza o signal _perfil
    }
  }

  alterarSenha(): void {
    if (this.formSenha.invalid) {
      this.formSenha.markAllAsTouched();
      return;
    }

    const { senhaAtual, novaSenha, confirmarSenha } = this.formSenha.value as {
      senhaAtual: string;
      novaSenha: string;
      confirmarSenha: string;
    };

    if (novaSenha !== confirmarSenha) {
      this._feedbackSenha.set({ texto: 'As senhas não coincidem.', erro: true });
      return;
    }

    const id = this.sessao.clienteId();
    if (id === null) {
      return;
    }

    const resultado = this.repo.alterarSenha({ clienteId: id, senhaAtual, novaSenha });
    this._feedbackSenha.set({ texto: resultado.mensagem, erro: !resultado.sucesso });

    if (resultado.sucesso) {
      this.formSenha.reset();
    }
  }

  limparFeedbacks(): void {
    this._feedbackPerfil.set(null);
    this._feedbackSenha.set(null);
  }

  // ── Helpers ─────────────────────────────────────────────────────
  private formatarCpf(cpf: number): string {
    const s = String(cpf).padStart(11, '0');
    return `${s.slice(0, 3)}.${s.slice(3, 6)}.${s.slice(6, 9)}-${s.slice(9)}`;
  }
}
