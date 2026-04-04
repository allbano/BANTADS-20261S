import { Component, Output, EventEmitter, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

/**
 * Modal para informar o motivo da rejeição (R11).
 * Emite o motivo digitado quando confirmado.
 */
@Component({
  selector: 'app-modal-rejeitar-motivo',
  imports: [FormsModule],
  templateUrl: './modal-rejeitar-motivo.html',
})
export class ModalRejeitarMotivo {
  @Output() confirmar = new EventEmitter<string>();
  @Output() cancelar = new EventEmitter<void>();

  motivo = signal('');

  onConfirmar(): void {
    const texto = this.motivo().trim();
    if (texto) {
      this.confirmar.emit(texto);
    }
  }

  onCancelar(): void {
    this.cancelar.emit();
  }
}
