import { Component, inject, Input } from '@angular/core';
import { Cliente } from '../../../../shared/models/cliente.model';
import { BsModalRef } from 'ngx-bootstrap/modal';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-modal-cliente',
  imports: [CommonModule],
  standalone: true,
  templateUrl: './modal-cliente.html',
  styleUrl: './modal-cliente.css',
})
export class ModalCliente {
  cliente!: Cliente;

  constructor(public BsModalRef : BsModalRef){  
  }

  closeModal(){
    this.BsModalRef.hide();
  }

}