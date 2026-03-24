import { Component, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-form-endereco',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './form-endereco.component.html',
  styleUrl: './form-endereco.component.css'
})
export class FormEnderecoComponent implements OnInit {
  @Output() proximo = new EventEmitter<any>();
  enderecoForm!: FormGroup;
  isSearchingCep = false;
  cepNotFound = false;

  constructor(private fb: FormBuilder) {}

  ngOnInit() {
    this.enderecoForm = this.fb.group({
      cep: ['', [Validators.required, Validators.pattern(/^\d{5}\-?\d{3}$/)]],
      logradouro: [{value: '', disabled: true}, Validators.required],
      numero: ['', Validators.required],
      complemento: [''],
      localidade: [{value: '', disabled: true}, Validators.required],
      uf: [{value: '', disabled: true}, Validators.required]
    });

    this.enderecoForm.get('cep')?.valueChanges.subscribe(cep => {
      const cleanCep = cep ? cep.replace(/\D/g, '') : '';
      if (cleanCep.length === 8) {
        this.buscarCep(cleanCep);
      }
    });
  }

  async buscarCep(cep: string) {
    this.isSearchingCep = true;
    this.cepNotFound = false;
    try {
      const response = await fetch(`https://viacep.com.br/ws/${cep}/json/`);
      const data = await response.json();
      
      if (data.erro) {
        this.cepNotFound = true;
        this.enderecoForm.patchValue({ logradouro: '', localidade: '', uf: '' });
      } else {
        this.enderecoForm.patchValue({
          logradouro: data.logradouro,
          localidade: data.localidade,
          uf: data.uf
        });
      }
    } catch (error) {
      this.cepNotFound = true;
    } finally {
      this.isSearchingCep = false;
    }
  }

  onSubmit() {
    if (this.enderecoForm.valid) {
      this.proximo.emit(this.enderecoForm.getRawValue());
    } else {
      this.enderecoForm.markAllAsTouched();
    }
  }
}
