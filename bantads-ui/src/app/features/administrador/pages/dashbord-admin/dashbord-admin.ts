import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-dashboard-admin',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashbord-admin.html'
})
export class DashbordAdmin {
  estatisticas = {
    totalClientes: 1250,
    totalGerentes: 15,
    totalMovimentacoes: 45890
  };

  constructor(private router: Router) {}

  sair() {
    if (confirm('Deseja realmente sair do sistema?')) {
      this.router.navigate(['/']); 
    }
  }
}