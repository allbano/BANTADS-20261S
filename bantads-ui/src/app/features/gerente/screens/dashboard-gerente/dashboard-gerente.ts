import { Component, inject, OnInit } from '@angular/core';
import { CurrencyPipe } from '@angular/common';

import { Cliente, ClienteRepository } from '../../../cliente/domain';
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

  ngOnInit() : void {
    // this.clientes = this.clienteRepository.listarTodos();
    this.clientes = [
      { id: 1, nome: 'Catharyna', cpf: 12912861012, email: 'cli1@bantads.com.br', senha: 'tads', salario: 10000 },
      { id: 2, nome: 'Cleuddônio', cpf: 19506382000, email: 'cli2@bantads.com.br', senha: 'tads', salario: 20000 },
      { id: 3, nome: 'Catianna', cpf: 85733854057, email: 'cli3@bantads.com.br', senha: 'tads', salario: 3000 },
      { id: 4, nome: 'Catianna', cpf: 58872160006, email: 'cli4@bantads.com.br', senha: 'tads', salario: 500 },
      { id: 5, nome: 'Coândrya', cpf: 76179646090, email: 'cli5@bantads.com.br', senha: 'tads', salario: 1500 },
    ];
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
