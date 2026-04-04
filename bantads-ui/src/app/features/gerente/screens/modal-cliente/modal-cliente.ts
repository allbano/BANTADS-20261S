import { Component, Input, Output, EventEmitter } from '@angular/core';
import type { Cliente } from '../../../cliente/domain';
import { CurrencyPipe } from '@angular/common';

@Component({
  selector: 'app-modal-cliente',
  imports: [CurrencyPipe],
  standalone: true,
  templateUrl: './modal-cliente.html'
})
export class ModalCliente {
  @Input() cliente: Cliente | null = null;
  @Output() close = new EventEmitter<void>();

  closeModal() {
    this.close.emit();
  }

  aprovarCliente() {
    console.log('Aprovando cliente:', this.cliente?.nome);
    this.close.emit();
  }
}