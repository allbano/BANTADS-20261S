import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CurrencyPipe } from '@angular/common';

import type { PedidoAutocadastro } from '../../domain/models/pedido-autocadastro.model';

/**
 * Modal de detalhes do pedido de autocadastro com opções de aprovar/recusar (R9/R10/R11).
 */
@Component({
  selector: 'app-modal-aprovar-rejeitar',
  imports: [CurrencyPipe],
  templateUrl: './modal-aprovar-rejeitar.html',
})
export class ModalAprovarRejeitar {
  @Input() pedido: PedidoAutocadastro | null = null;
  @Output() aprovar = new EventEmitter<string>();
  @Output() rejeitar = new EventEmitter<string>();
  @Output() fechar = new EventEmitter<void>();

  onAprovar(): void {
    if (this.pedido) {
      this.aprovar.emit(this.pedido.cpf);
    }
  }

  onRejeitar(): void {
    if (this.pedido) {
      this.rejeitar.emit(this.pedido.cpf);
    }
  }

  onFechar(): void {
    this.fechar.emit();
  }
}
