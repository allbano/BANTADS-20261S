import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { DadosPessoais, Endereco } from '../../domain/models/autocadastro.model';

@Component({
  selector: 'app-resumo-confirmacao-ddd',
  standalone: true,
  imports: [CurrencyPipe],
  templateUrl: './resumo-confirmacao.component.html',
  styleUrl: './resumo-confirmacao.component.css'
})
export class ResumoConfirmacaoComponent {
  @Input() dadosPessoais: DadosPessoais | null = null;
  @Input() endereco: Endereco | null = null;
  @Input() limiteEstimado: number = 0;
  @Input() isCarregando: boolean = false;

  @Output() editarDadosPessoais = new EventEmitter<void>();
  @Output() editarEndereco = new EventEmitter<void>();
  @Output() confirmar = new EventEmitter<void>();
  @Output() voltar = new EventEmitter<void>();

  onEditarDadosPessoais() {
    this.editarDadosPessoais.emit();
  }
  onEditarEndereco() {
    this.editarEndereco.emit();
  }
  onConfirmar() {
    this.confirmar.emit();
  }
  onVoltar() {
    this.voltar.emit();
  }
}
