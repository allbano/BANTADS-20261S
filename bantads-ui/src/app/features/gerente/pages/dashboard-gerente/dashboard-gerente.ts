import { Component, inject, OnInit } from '@angular/core';
import { ClienteService } from '../../../../services/cliente.service';
import { Cliente } from '../../../../shared/models/cliente.model';
import { CommonModule } from '@angular/common';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { ModalCliente } from '../modal-cliente/modal-cliente';


@Component({
  selector: 'app-dashboard-gerente',
  imports: [ CommonModule],
  standalone: true,
  templateUrl: './dashboard-gerente.html',
  styleUrl: './dashboard-gerente.css',
})
export class DashboardGerente implements OnInit {

  BsModalRef?: BsModalRef;

  constructor(private modalService : BsModalService){

  }

  private clienteService = inject(ClienteService);
  clientes: Cliente[] = [];

  ngOnInit() : void {
    // this.clientes = this.clienteService.listarTodos();
    this.clientes = [
      new Cliente (1, "Catharyna", 12912861012, "cli1@bantads.com.br", "tads", 10000 ),
      new Cliente (2, "Cleuddônio", 19506382000, "cli2@bantads.com.br", "tads", 20000 ),
      new Cliente (3, "Catianna", 85733854057, "cli3@bantads.com.br", "tads", 3000 ),
      new Cliente (4, "Catianna", 58872160006, "cli4@bantads.com.br", "tads", 500 ),
      new Cliente (5, "Coândrya", 76179646090, "cli5@bantads.com.br", "tads", 1500 )
    ]
  } 

  openModal(cliente: Cliente) {
    const initialState = {
      cliente: cliente
    };
    this.modalService.show(ModalCliente, { initialState });
  }

  //abrirModalPessoa(cliente: Cliente) {
  //  const modalRef = this.modalService.open(ModalCliente);
  //  modalRef.componentInstance.cliente = cliente;
  //}
  
}
