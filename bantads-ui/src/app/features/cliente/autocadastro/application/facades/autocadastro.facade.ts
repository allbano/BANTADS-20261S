import { Injectable, computed, inject, signal } from '@angular/core';
import { AutocadastroRepository } from '../../domain/repositories/autocadastro.repository';
import { DadosPessoais, Endereco, AutocadastroPayload } from '../../domain/models/autocadastro.model';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AutocadastroFacade {
  private repository = inject(AutocadastroRepository);

  // Estado interno com Signals
  private _passoAtual = signal<number>(1); // 1 = Pessoais, 2 = Endereço, 3 = Resumo, 4 = Finalizando (Sucesso)
  private _dadosPessoais = signal<DadosPessoais | null>(null);
  private _endereco = signal<Endereco | null>(null);
  private _isCarregando = signal<boolean>(false);

  // Computeds e getters para acesso read-only da Views
  passoAtual = computed(() => this._passoAtual());
  dadosPessoais = computed(() => this._dadosPessoais());
  endereco = computed(() => this._endereco());
  isCarregando = computed(() => this._isCarregando());

  // Lógica de limite simulado de 50%
  limiteEstimado = computed(() => {
    const salario = this._dadosPessoais()?.salario;
    return salario ? salario * 0.5 : 0;
  });

  // Ações
  setPassoAtual(passo: number) {
    this._passoAtual.set(passo);
  }

  avancarDadosPessoais(dados: DadosPessoais) {
    this._dadosPessoais.set(dados);
    this.setPassoAtual(2);
  }

  avancarEndereco(dados: Endereco) {
    this._endereco.set(dados);
    this.setPassoAtual(3);
  }

  // Apenas edita dados pessoais, retrocedendo do resumo para o passo 1
  voltarEditarDadosPessoais() {
    this.setPassoAtual(1);
  }

  voltarEditarEndereco() {
    this.setPassoAtual(2);
  }

  // Retrocede pelo "Voltar" padrão (ex: Passo 2 -> 1, Passo 3 -> 2)
  voltarStep() {
    const p = this._passoAtual();
    if (p > 1) {
      this.setPassoAtual(p - 1);
    }
  }

  confirmarESolicitarCadastro() {
    this._isCarregando.set(true);

    const payload: AutocadastroPayload = {
      dadosPessoais: this._dadosPessoais()!,
      endereco: this._endereco()!
    };

    this.repository.solicitarCadastro(payload).subscribe({
      next: () => {
        this._isCarregando.set(false);
        this.setPassoAtual(4); // Exibe Sucesso
      },
      error: (err) => {
        console.error('Erro ao processar o cadastro', err);
        this._isCarregando.set(false);
      }
    });
  }

  reset() {
    this._passoAtual.set(1);
    this._dadosPessoais.set(null);
    this._endereco.set(null);
    this._isCarregando.set(false);
  }
}
