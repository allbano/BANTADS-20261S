import { Component, EventEmitter, Input, OnInit, Output, inject } from '@angular/core';

import { RouterLink } from '@angular/router';
import {
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  Validators,
  AbstractControl,
  AsyncValidatorFn,
  ValidationErrors,
} from '@angular/forms';
import { Observable, timer, of } from 'rxjs';
import { switchMap, map } from 'rxjs/operators';
import { DadosPessoais } from '../../domain/models/autocadastro.model';
import { ClienteLocalStorageService } from '../../infrastructure/services/cliente-local-storage.service';
import { CLIENTES_DEMONSTRACAO } from '../../infrastructure/data/clientes-demonstracao';
import { AprovacaoMockService } from '../../../gerente/infrastructure/services/aprovacao-mock.service';

// Função de formatação de CPF
export function formatarCPF(value: string): string {
  const cpf = value.replace(/\D/g, '');
  if (cpf.length <= 11) {
    return cpf
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d)/, '$1.$2')
      .replace(/(\d{3})(\d{1,2})$/, '$1-$2');
  }
  return cpf.substring(0, 11);
}

@Component({
  selector: 'app-form-dados-pessoais-ddd',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './form-dados-pessoais.component.html',
  styleUrl: './form-dados-pessoais.component.css'
})
export class FormDadosPessoaisComponent implements OnInit {
  @Input() initialData: DadosPessoais | null = null;
  @Output() proximo = new EventEmitter<DadosPessoais>();

  form!: FormGroup;

  private clienteStorage = inject(ClienteLocalStorageService);
  private aprovacaoMock = inject(AprovacaoMockService);

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      nome: [this.initialData?.nome || '', [Validators.required, Validators.minLength(3)]],
      cpf: [
        this.initialData?.cpf || '',
        [Validators.required, Validators.pattern(/^\d{3}\.?\d{3}\.?\d{3}\-?\d{2}$/)],
        [this.cpfValidator()],
      ],
      telefone: [this.initialData?.telefone || '', [Validators.required]],
      email: [this.initialData?.email || '', [Validators.required, Validators.email]],
      salario: [this.initialData?.salario || '', [Validators.required, Validators.min(0)]],
    });
  }

  // Validador de CPF como método do componente
  cpfValidator(): AsyncValidatorFn {
    return (control: AbstractControl): Observable<ValidationErrors | null> => {
      return timer(500).pipe(
        switchMap(() => {
          const cpfOriginal = control.value;
          if (!cpfOriginal) return of(null);

          const cpf = cpfOriginal.replace(/\D/g, '');
          if (cpf.length !== 11 || !!cpf.match(/(\d)\1{10}/)) {
            return of({ cpfInvalido: true });
          }

          const calc = (n: number) => {
            let s = 0;
            for (let i = 0; i < n; i++) s += parseInt(cpf[i]) * (n + 1 - i);
            let r = 11 - (s % 11);
            return r > 9 ? 0 : r;
          };

          if (calc(9) !== parseInt(cpf[9]) || calc(10) !== parseInt(cpf[10])) {
            return of({ cpfInvalido: true });
          }

          // Verificação real de CPF duplicado contra as bases de dados
          const cpfNum = Number(cpf);

          // Verifica em clientes de demonstração
          const existeDemo = CLIENTES_DEMONSTRACAO.some(c => c.cpf === cpfNum);

          // Verifica no storage local (clientes aprovados)
          const existeStorage = this.clienteStorage.listarTodos().some(c => c.cpf === cpfNum);

          // Verifica em pedidos pendentes
          const existePendente = this.aprovacaoMock.listarPendentes().some(p =>
            p.cpf.replace(/\D/g, '') === cpf
          );

          console.log('Validação CPF:', { cpf, cpfNum, existeDemo, existeStorage, existePendente });

          if (existeDemo || existeStorage || existePendente) {
            return of({ cpfDuplicado: true });
          }

          return of(null);
        }),
      );
    };
  }

  // Método para formatar o CPF durante a digitação
  onCpfInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    const value = input.value;
    input.value = formatarCPF(value);
    this.form.get('cpf')?.setValue(input.value, { emitEvent: false });
  }

  onSubmit() {
    if (this.form.valid) {
      this.proximo.emit(this.form.value as DadosPessoais);
    } else {
      this.form.markAllAsTouched();
    }
  }
}
