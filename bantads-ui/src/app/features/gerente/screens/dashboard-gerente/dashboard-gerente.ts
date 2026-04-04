import { Component, inject, OnInit } from '@angular/core';
import { CurrencyPipe } from '@angular/common';

import { Cliente, ClienteRepository } from '../../../cliente/domain';
import { CLIENTES_DEMONSTRACAO } from '../../../cliente/infrastructure/data/clientes-demonstracao';
import { ClienteLocalStorageService } from '../../../cliente/infrastructure/services/cliente-local-storage.service';
import { ModalCliente } from '../modal-cliente/modal-cliente';


@Component({
  selector: 'app-dashboard-gerente',
  imports: [ CurrencyPipe, ModalCliente],
  standalone: true,
  templateUrl: './dashboard-gerente.html',
  styleUrl: './dashboard-gerente.css',
  providers: [
    { provide: ClienteRepository, useClass: ClienteLocalStorageService },
  ],
})
export class DashboardGerente implements OnInit {

  isModalOpen = false;
  selectedCliente: Cliente | null = null;

  private clienteRepository = inject(ClienteRepository);
  clientes: Cliente[] = [];

  ngOnInit(): void {
    // this.clientes = this.clienteRepository.listarTodos();
    this.clientes = [...CLIENTES_DEMONSTRACAO];
  }

  openModal(cliente: Cliente) {
    this.selectedCliente = cliente;
    this.isModalOpen = true;
  }

  closeModal() {
    this.isModalOpen = false;
    this.selectedCliente = null;
  }
}
