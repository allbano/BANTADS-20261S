import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms'; // <--- IMPORTANTE

interface ClienteRelatorio {
  id: number;
  nome: string;
  cpf: string;
  gerente: string;
  saldo: number;
  status: 'ativos' | 'inativos';
}

@Component({
  selector: 'app-relatorio',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule], 
  templateUrl: './relatorio.html'
})
export class Relatorio implements OnInit {
  termoBusca: string = '';
  statusFiltro: string = '';

  clientes: ClienteRelatorio[] = [
    { id: 1, nome: 'Mariana Costa', cpf: '123.456.789-00', gerente: 'Ana Souza', saldo: 15420.00, status: 'ativos' },
    { id: 2, nome: 'João Pedro', cpf: '222.333.444-55', gerente: 'Carlos Silva', saldo: 250.50, status: 'ativos' },
    { id: 3, nome: 'Fernanda Alves', cpf: '777.888.999-11', gerente: 'Ana Souza', saldo: -150.00, status: 'inativos' },
    { id: 4, nome: 'Lucas Marques', cpf: '000.111.222-33', gerente: 'Beatriz Lima', saldo: 5000.00, status: 'ativos' }
  ];

  clientesFiltrados: ClienteRelatorio[] = [];

  constructor(private router: Router) {}

  ngOnInit() {
    // Ao iniciar a tela, mostra todos
    this.clientesFiltrados = [...this.clientes];
  }

  filtrar() {
    this.clientesFiltrados = this.clientes.filter(cliente => {
      const matchTexto = cliente.nome.toLowerCase().includes(this.termoBusca.toLowerCase()) || 
                         cliente.cpf.includes(this.termoBusca);
      
      const matchStatus = this.statusFiltro ? cliente.status === this.statusFiltro : true;
      
      return matchTexto && matchStatus;
    });
  }

  sair() {
    if (confirm('Deseja realmente sair?')) {
      this.router.navigate(['/']);
    }
  }
}