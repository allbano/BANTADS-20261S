import { Component, EventEmitter, Input, OnInit, Output, inject, signal } from '@angular/core';

import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Endereco } from '../../domain/models/autocadastro.model';

@Component({
  selector: 'app-form-endereco-ddd',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './form-endereco.component.html',
  styleUrl: './form-endereco.component.css'
})
export class FormEnderecoComponent implements OnInit {
  @Input() initialData: Endereco | null = null;
  @Output() proximo = new EventEmitter<Endereco>();
  @Output() voltar = new EventEmitter<void>();

  form!: FormGroup;
  buscandoCep = signal(false);
  cepInvalido = signal(false);

  private http = inject(HttpClient);

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      cep: [this.initialData?.cep || '', [Validators.required]],
      logradouro: [
        { value: this.initialData?.logradouro || '', disabled: true },
        [Validators.required],
      ],
      numero: [this.initialData?.numero || '', [Validators.required]],
      complemento: [this.initialData?.complemento || ''],
      cidade: [{ value: this.initialData?.cidade || '', disabled: true }, [Validators.required]],
      uf: [
        { value: this.initialData?.uf || '', disabled: true },
        [Validators.required, Validators.minLength(2), Validators.maxLength(2)],
      ],
    });
  }

  buscarCep() {
    const cep = this.form.get('cep')?.value;
    if (!cep) return;

    const cepClean = cep.replace(/\D/g, '');
    if (cepClean.length === 8) {
      this.buscandoCep.set(true);
      this.cepInvalido.set(false);
      this.http.get<any>(`https://viacep.com.br/ws/${cepClean}/json/`).subscribe({
        next: (dados) => {
          this.buscandoCep.set(false);
          if (dados.erro) {
            this.cepInvalido.set(true);
            this.form.patchValue({ logradouro: '', cidade: '', uf: '' });
          } else {
            this.form.patchValue({
              logradouro: dados.logradouro,
              cidade: dados.localidade,
              uf: dados.uf,
            });
            // Opcionalmente focar no número
          }
        },
        error: () => {
          this.buscandoCep.set(false);
          this.cepInvalido.set(true);
        },
      });
    }
  }

  onSubmit() {
    if (this.form.valid) {
      // getRawValue() gets the values of disabled fields too, value() ignores disabled.
      this.proximo.emit(this.form.getRawValue() as Endereco);
    } else {
      this.form.markAllAsTouched();
    }
  }

  onVoltar() {
    this.voltar.emit();
  }
}
