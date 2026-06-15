import { Injectable, computed, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

import type { DashboardClienteResumo } from '../../domain/models/dashboard-cliente-resumo.model';
import type { PerfilCliente } from '../../domain/models/perfil-cliente.model';
import { DashboardClienteRepository } from '../../domain/repositories/dashboard-cliente.repository';
import { PerfilClienteRepository } from '../../domain/repositories/perfil-cliente.repository';

export type FeedbackPerfil = { texto: string; erro: boolean };

/**
 * Façade da tela "Meu Perfil" (R4).
 *
 * - Carrega o perfil + resumo da conta do cliente autenticado via API Gateway
 * - Expõe Signals reativos para o componente consumir
 * - Orquestra o caso de uso salvarPerfil() (PUT /clientes/{cpf} — SAGA)
 *
 * A troca de senha não é exposta pela Gateway para clientes, portanto não há esse fluxo aqui.
 */
@Injectable()
export class PerfilClienteFacade {
  private readonly repo = inject(PerfilClienteRepository);
  private readonly contaRepo = inject(DashboardClienteRepository);
  private readonly fb = inject(FormBuilder);

  // ── Estado reativo ──────────────────────────────────────────────
  private readonly _perfil = signal<PerfilCliente | null>(null);
  private readonly _resumoConta = signal<DashboardClienteResumo | null>(null);
  private readonly _feedbackPerfil = signal<FeedbackPerfil | null>(null);
  private readonly _carregando = signal(false);

  readonly perfil = this._perfil.asReadonly();
  readonly resumoConta = this._resumoConta.asReadonly();
  readonly feedbackPerfil = this._feedbackPerfil.asReadonly();
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

  // ── Casos de uso ────────────────────────────────────────────────

  carregar(): void {
    this._carregando.set(true);

    this.repo.buscarPerfil().subscribe({
      next: (perfil) => {
        this._carregando.set(false);
        this._perfil.set(perfil);
        this.formDados.patchValue({
          nome: perfil.nome,
          email: perfil.email,
          telefone: perfil.telefone,
          cpf: this.formatarCpf(perfil.cpf),
          salario: perfil.salario,
        });
        this.formEndereco.patchValue({ ...perfil.endereco });
      },
      error: () => {
        this._carregando.set(false);
        this._perfil.set(null);
      },
    });

    this.contaRepo.obterResumo().subscribe({
      next: (resumo) => this._resumoConta.set(resumo),
      error: () => this._resumoConta.set(null),
    });
  }

  salvarPerfil(): void {
    if (this.formDados.invalid || this.formEndereco.invalid) {
      this.formDados.markAllAsTouched();
      this.formEndereco.markAllAsTouched();
      return;
    }

    const atual = this._perfil();
    if (!atual) {
      return;
    }

    const payload: PerfilCliente = {
      cpf: atual.cpf,
      nome: this.formDados.getRawValue().nome,
      email: this.formDados.getRawValue().email,
      salario: Number(this.formDados.getRawValue().salario) || 0,
      telefone: this.formDados.getRawValue().telefone,
      endereco: this.formEndereco.getRawValue(),
    };

    this.repo.salvarPerfil(payload).subscribe((resultado) => {
      this._feedbackPerfil.set({ texto: resultado.mensagem, erro: !resultado.sucesso });
      if (resultado.sucesso) {
        this.carregar();
      }
    });
  }

  limparFeedbacks(): void {
    this._feedbackPerfil.set(null);
  }

  // ── Helpers ─────────────────────────────────────────────────────
  private formatarCpf(cpf: string): string {
    const s = String(cpf).replace(/\D/g, '').padStart(11, '0');
    return `${s.slice(0, 3)}.${s.slice(3, 6)}.${s.slice(6, 9)}-${s.slice(9)}`;
  }
}
