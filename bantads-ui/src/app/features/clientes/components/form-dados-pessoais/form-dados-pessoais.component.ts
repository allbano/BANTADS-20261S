import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, AsyncValidatorFn, ValidationErrors } from '@angular/forms';
import { Observable, timer, of } from 'rxjs';
import { switchMap } from 'rxjs/operators';

export function cpfValidator(): AsyncValidatorFn {
  return (control: AbstractControl): Observable<ValidationErrors | null> => {
    return timer(500).pipe(
      switchMap(() => {
        const cpf = control.value;
        if (cpf && cpf.startsWith('111')) {
          return of({ cpfDuplicado: true });
        }
        return of(null);
      })
    );
  };
}

@Component({
  selector: 'app-form-dados-pessoais',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './form-dados-pessoais.component.html',
  styleUrl: './form-dados-pessoais.component.css',
})
export class FormDadosPessoaisComponent implements OnInit {
  @Output() proximo = new EventEmitter<any>();
  registrationForm!: FormGroup;

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    this.registrationForm = this.fb.group({
      nome: ['', [Validators.required, Validators.minLength(3)]],
      cpf: ['', [Validators.required, Validators.pattern(/^\d{3}\.?\d{3}\.?\d{3}\-?\d{2}$/)], [cpfValidator()]],
      telefone: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      salario: ['', [Validators.required]]
    });
  }

  onSubmit() {
    if (this.registrationForm.valid) {
      this.proximo.emit(this.registrationForm.value);
    } else {
      this.registrationForm.markAllAsTouched();
    }
  }
}
