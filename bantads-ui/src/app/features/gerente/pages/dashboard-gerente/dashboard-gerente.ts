import { AfterViewInit, Component, TemplateRef, ViewChild } from '@angular/core';
import { Table } from "../../../../shared/components/table/table";


@Component({
  selector: 'app-dashboard-gerente',
  imports: [Table],
  templateUrl: './dashboard-gerente.html',
  styleUrl: './dashboard-gerente.css',
})
export class DashboardGerente implements AfterViewInit{

  @ViewChild('acoes', { static: true }) acoesTemplate!: TemplateRef<any>;

  usuarios = [
    { id: 1, nome: 'Matheus', idade: 20 },
    { id: 2, nome: 'Ana', idade: 25 },
  ];

  colunas: any[] = [];

  ngAfterViewInit(): void {
  setTimeout(() => {
    this.colunas = [
      { id: 'nome', header: 'Nome' },
      { id: 'idade', header: 'Idade' },
      { id: 'acoes', header: 'Ações', render: this.acoesTemplate }
    ];
  });
  }

  onAction(event: any) {
    console.log('Evento da tabela:', event);
  }
}
