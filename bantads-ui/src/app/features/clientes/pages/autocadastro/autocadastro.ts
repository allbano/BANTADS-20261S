import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormDadosPessoaisComponent } from '../../components/form-dados-pessoais/form-dados-pessoais.component';
import { FormEnderecoComponent } from '../../components/form-endereco/form-endereco.component';

@Component({
  selector: 'app-autocadastro',
  standalone: true,
  imports: [CommonModule, FormDadosPessoaisComponent, FormEnderecoComponent],
  templateUrl: './autocadastro.html',
  styleUrl: './autocadastro.css',
})
export class AutocadastroComponent {
  passoAtual = signal<number>(1);
  dadosCadastro = signal<any>({});
  isFinalizando = signal<boolean>(false);

  avancarPasso(dados: any) {
    this.dadosCadastro.update(d => ({ ...d, ...dados }));
    this.passoAtual.update(p => p + 1);
  }

  async finalizarCadastro(dadosEndereco: any) {
    this.dadosCadastro.update(d => ({ ...d, ...dadosEndereco }));
    this.isFinalizando.set(true);

    console.log('Enviando Payload Finalizando (Fire and Forget):', this.dadosCadastro());
    
    // Simulando tempo de resposta da API
    await new Promise(resolve => setTimeout(resolve, 1500));
    
    this.isFinalizando.set(false);
    this.passoAtual.set(3); // Tela de SUCESSO
  }
}
