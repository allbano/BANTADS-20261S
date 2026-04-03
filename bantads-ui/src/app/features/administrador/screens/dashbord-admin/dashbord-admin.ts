import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';

interface GerenteDashboard {
  id: number;
  nome: string;
  qtdClientes: number;
  saldoPositivo: number;
  saldoNegativo: number;
}

@Component({
  selector: 'app-dashboard-admin',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashbord-admin.html'
})
export class DashboardAdminComponent implements OnInit {
  
  // Dados mockados conforme o requisito R15
  gerentesEstatisticas: GerenteDashboard[] = [
    { id: 1, nome: 'Ana Souza', qtdClientes: 45, saldoPositivo: 150500.50, saldoNegativo: -5400.00 },
    { id: 2, nome: 'Carlos Silva', qtdClientes: 32, saldoPositivo: 85000.00, saldoNegativo: -12000.50 },
    { id: 3, nome: 'Beatriz Lima', qtdClientes: 12, saldoPositivo: 250000.75, saldoNegativo: 0.00 },
    { id: 4, nome: 'Roberto Alves', qtdClientes: 28, saldoPositivo: 42000.20, saldoNegativo: -800.00 },
    { id: 5, nome: 'Fernanda Costa', qtdClientes: 50, saldoPositivo: 0.00, saldoNegativo: -15000.00 }
  ];

  constructor(private router: Router) {}

  ngOnInit() {
    // REQUISITO R15: Deve ser mostrado os gerentes com maiores saldos positivos primeiro
    this.gerentesEstatisticas.sort((a, b) => b.saldoPositivo - a.saldoPositivo);
  }

  sair() {
    if (confirm('Deseja realmente sair do sistema?')) {
      this.router.navigate(['/']);
    }
  }
}