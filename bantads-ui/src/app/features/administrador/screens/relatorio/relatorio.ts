import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

interface ClienteRelatorio {
  cpf: string;
  nome: string;
  email: string;
  salario: number;
  numeroConta: string;
  saldo: number;
  limite: number;
  cpfGerente: string;
  nomeGerente: string;
}

@Component({
  selector: 'app-relatorio',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './relatorio.html'
})
export class RelatorioComponent implements OnInit {
  termoBusca: string = '';

  // Dados mockados conforme o Requisito R16
  clientes: ClienteRelatorio[] = [
    { 
      cpf: '123.456.789-00', nome: 'Mariana Costa', email: 'mari@email.com', 
      salario: 5000, numeroConta: '001-X', saldo: 15420.50, limite: 2000, 
      cpfGerente: '111.222.333-44', nomeGerente: 'Ana Souza' 
    },
    { 
      cpf: '222.333.444-55', nome: 'João Pedro', email: 'joao@email.com', 
      salario: 3500, numeroConta: '002-Y', saldo: 250.00, limite: 500, 
      cpfGerente: '555.666.777-88', nomeGerente: 'Carlos Silva' 
    },
    { 
      cpf: '000.111.222-33', nome: 'Beatriz Alves', email: 'bea@email.com', 
      salario: 8000, numeroConta: '003-Z', saldo: -150.00, limite: 5000, 
      cpfGerente: '111.222.333-44', nomeGerente: 'Ana Souza' 
    }
  ];

  clientesFiltrados: ClienteRelatorio[] = [];

  constructor(private router: Router) {}

  ngOnInit() {
    // REQUISITO R16: Ordenar de forma crescente por nome do cliente
    this.clientes.sort((a, b) => a.nome.localeCompare(b.nome));
    this.clientesFiltrados = [...this.clientes];
  }

  filtrar() {
    const termo = this.termoBusca.toLowerCase();
    this.clientesFiltrados = this.clientes.filter(c => 
      c.nome.toLowerCase().includes(termo) || 
      c.cpf.includes(termo) || 
      c.email.toLowerCase().includes(termo)
    );
  }

  sair() {
    if (confirm('Deseja sair do sistema?')) {
      this.router.navigate(['/']);
    }
  }
}