import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

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
import { switchMap } from 'rxjs/operators';
import { DadosPessoais } from '../../domain/models/autocadastro.model';

export function cpfValidator(): AsyncValidatorFn {
  return (control: AbstractControl): Observable<ValidationErrors | null> => {
    return timer(500).pipe(
      switchMap(() => {
        const cpfOriginal = control.value;
        if (!cpfOriginal) return of(null);

        const cpf = cpfOriginal.replace(/[^\d]+/g, '');
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

        // Simulação: CPF começando com '111' dá erro de duplicado
        if (cpf.startsWith('111')) {
          return of({ cpfDuplicado: true });
        }

        return of(null);
      }),
    );
  };
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

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      nome: [this.initialData?.nome || '', [Validators.required, Validators.minLength(3)]],
      cpf: [
        this.initialData?.cpf || '',
        [Validators.required, Validators.pattern(/^\d{3}\.?\d{3}\.?\d{3}\-?\d{2}$/)],
        [cpfValidator()],
      ],
      telefone: [this.initialData?.telefone || '', [Validators.required]],
      email: [this.initialData?.email || '', [Validators.required, Validators.email]],
      salario: [this.initialData?.salario || '', [Validators.required, Validators.min(0)]],
    });
  }

  onSubmit() {
    if (this.form.valid) {
      this.proximo.emit(this.form.value as DadosPessoais);
    } else {
      this.form.markAllAsTouched();
    }
  }
}
