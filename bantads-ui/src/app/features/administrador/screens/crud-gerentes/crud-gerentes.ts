import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

interface Gerente {
  id: number;
  nome: string;
  email: string;
  cpf: string;
  telefone: string;
  senha?: string;
  clientes: number;
  saldoPositivo: number; // Usado para o critério de desempate do R17
}

@Component({
  selector: 'app-crud-gerentes',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './crud-gerentes.html'
})
export class CrudGerentesComponent implements OnInit {
  // Mock inicial
  gerentes: Gerente[] = [
    { id: 1, nome: 'Mariana Costa', email: 'mariana@bantads.com', cpf: '111.222.333-44', telefone: '(11) 98888-1111', clientes: 45, saldoPositivo: 15000.00, senha: '123' },
    { id: 2, nome: 'Carlos Silva', email: 'carlos@bantads.com', cpf: '555.666.777-88', telefone: '(21) 97777-2222', clientes: 45, saldoPositivo: 5000.00, senha: '123' },
    { id: 3, nome: 'Beatriz Lima', email: 'beatriz@bantads.com', cpf: '999.888.777-66', telefone: '(31) 96666-3333', clientes: 12, saldoPositivo: 25000.00, senha: '123' }
  ];

  // Controle do Modal
  exibirModal: boolean = false;
  modoModal: 'inserir' | 'editar' = 'inserir';
  gerenteForm: Partial<Gerente> = {};

  constructor(private router: Router) {}

  ngOnInit() {
    this.ordenarGerentes(); // R19: Ordenar na inicialização
  }

  // R19: Ordenar de forma crescente por nome
  ordenarGerentes() {
    this.gerentes.sort((a, b) => a.nome.localeCompare(b.nome));
  }

  abrirModalNovo() {
    this.modoModal = 'inserir';
    this.gerenteForm = { clientes: 0, saldoPositivo: 0 }; // Reseta form
    this.exibirModal = true;
  }

  abrirModalEdicao(gerente: Gerente) {
    this.modoModal = 'editar';
    // R20: Clona os dados para edição (somente Nome, E-mail e Senha poderão ser alterados no HTML)
    this.gerenteForm = { ...gerente }; 
    this.exibirModal = true;
  }

  fecharModal() {
    this.exibirModal = false;
    this.gerenteForm = {};
  }

  salvarGerente() {
    if (this.modoModal === 'inserir') {
      // R17: Lógica de Inserção e distribuição de contas
      let contasParaNovo = 0;

      if (this.gerentes.length > 0) {
        // Encontra a quantidade máxima de contas
        const maxContas = Math.max(...this.gerentes.map(g => g.clientes));
        
        // Pega todos os gerentes que têm essa quantidade máxima
        const candidatos = this.gerentes.filter(g => g.clientes === maxContas);
        
        // Desempate: Gerente com MENOR saldo positivo
        const doador = candidatos.reduce((prev, curr) => (prev.saldoPositivo < curr.saldoPositivo) ? prev : curr);

        // Regra de exceção R17: Se for o primeiro (já tratado pelo length) ou se só houver 1 e ele tiver só 1 conta
        if (this.gerentes.length === 1 && doador.clientes <= 1) {
          contasParaNovo = 0; // Fica sem conta
        } else if (doador.clientes > 0) {
          doador.clientes -= 1; // Tira uma conta do doador
          contasParaNovo = 1;   // Dá a conta para o novo
        }
      }

      const novo: Gerente = {
        id: Date.now(),
        nome: this.gerenteForm.nome!,
        email: this.gerenteForm.email!,
        cpf: this.gerenteForm.cpf!,
        telefone: this.gerenteForm.telefone!,
        senha: this.gerenteForm.senha!,
        clientes: contasParaNovo,
        saldoPositivo: 0
      };
      
      this.gerentes.push(novo);

    } else {
      // Edição
      const index = this.gerentes.findIndex(g => g.id === this.gerenteForm.id);
      if (index !== -1) {
        // R20: Atualiza apenas os campos permitidos
        this.gerentes[index].nome = this.gerenteForm.nome!;
        this.gerentes[index].email = this.gerenteForm.email!;
        if (this.gerenteForm.senha) {
          this.gerentes[index].senha = this.gerenteForm.senha;
        }
      }
    }

    this.ordenarGerentes();
    this.fecharModal();
  }

  // R18: Lógica de Remoção
  excluirGerente(id: number) {
    if (this.gerentes.length <= 1) {
      alert('Ação bloqueada: Não é permitido remover o último gerente do banco.');
      return;
    }

    if (confirm('Tem certeza que deseja excluir este gerente?')) {
      const gerenteRemovido = this.gerentes.find(g => g.id === id);
      const outrosGerentes = this.gerentes.filter(g => g.id !== id);

      if (gerenteRemovido && gerenteRemovido.clientes > 0) {
        // Encontra o gerente que possui MENOS contas no momento
        const recebedor = outrosGerentes.reduce((prev, curr) => (prev.clientes < curr.clientes) ? prev : curr);
        recebedor.clientes += gerenteRemovido.clientes; // Transfere as contas
        alert(`O gerente foi excluído. ${gerenteRemovido.clientes} conta(s) foram transferidas para ${recebedor.nome}.`);
      }

      this.gerentes = outrosGerentes;
      this.ordenarGerentes();
    }
  }

  sair() {
    if (confirm('Deseja sair do sistema?')) {
      this.router.navigate(['/']);
    }
  }
}