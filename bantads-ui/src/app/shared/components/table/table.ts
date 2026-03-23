import { Component, EventEmitter, Input, Output, TemplateRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface TableColumn {
  id: string;
  header: string;
  render?: TemplateRef<any>;
  cellStyle?: string;
  headerStyle?: string;
}

@Component({
  selector: 'app-table',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './table.html',
  styleUrl: './table.css',
})

export class Table {

  @Input() colunas: any[] = [];
  @Input() linhas: any[] = [];

  @Output() action = new EventEmitter<any>();
}
