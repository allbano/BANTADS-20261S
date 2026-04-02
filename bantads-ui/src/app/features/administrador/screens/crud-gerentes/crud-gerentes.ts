import { Component } from '@angular/core';

import { Router, RouterLink } from '@angular/router';

interface Gerente {
  id: number;
  nome: string;
  email: string;
  cpf: string;
  clientes: number;
}

@Component({
  selector: 'app-crud-gerentes',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './crud-gerentes.html',
})
export class CrudGerentes {
  gerentes: Gerente[] = [
    {
      id: 1,
      nome: 'Ana Souza',
      email: 'ana.souza@bantads.com',
      cpf: '111.222.333-44',
      clientes: 45,
    },
    {
      id: 2,
      nome: 'Carlos Silva',
      email: 'carlos.silva@bantads.com',
      cpf: '555.666.777-88',
      clientes: 32,
    },
    {
      id: 3,
      nome: 'Beatriz Lima',
      email: 'beatriz.lima@bantads.com',
      cpf: '999.888.777-66',
      clientes: 12,
    },
  ];

  constructor(private router: Router) {}

  novoGerente() {
    const nome = prompt('Digite o nome do novo gerente:');
    if (nome) {
      const novo: Gerente = {
        id: Date.now(),
        nome: nome,
        email: `${nome.toLowerCase().replace(/\s/g, '.')}@bantads.com`,
        cpf: '000.000.000-00',
        clientes: 0,
      };
      this.gerentes.push(novo);
    }
  }

  editarGerente(gerente: Gerente) {
    const novoNome = prompt('Editar nome do gerente:', gerente.nome);
    if (novoNome) {
      gerente.nome = novoNome;
    }
  }

  excluirGerente(id: number) {
    if (confirm('Tem certeza que deseja excluir este gerente?')) {
      this.gerentes = this.gerentes.filter((g) => g.id !== id);
    }
  }

  sair() {
    if (confirm('Deseja realmente sair?')) {
      this.router.navigate(['/']);
    }
  }
}
