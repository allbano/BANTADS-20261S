import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';

import { FormDadosPessoaisComponent } from '../../components/form-dados-pessoais/form-dados-pessoais.component';
import { FormEnderecoComponent } from '../../components/form-endereco/form-endereco.component';
import { ResumoConfirmacaoComponent } from '../../components/resumo-confirmacao/resumo-confirmacao.component';
import { AutocadastroFacade } from '../../../application/facades/autocadastro.facade';
import { AutocadastroRepository } from '../../../domain/repositories/autocadastro.repository';
import { AutocadastroApiService } from '../../../infrastructure/services/autocadastro-api.service';

@Component({
  selector: 'app-autocadastro-ddd',
//  standalone: true,
  imports: [RouterLink, FormDadosPessoaisComponent, FormEnderecoComponent, ResumoConfirmacaoComponent],
  templateUrl: './autocadastro.html',
  styleUrl: './autocadastro.css',
  providers: [
    AutocadastroFacade,
    { provide: AutocadastroRepository, useClass: AutocadastroApiService },
  ],
})
export class AutocadastroComponent {
  public facade = inject(AutocadastroFacade);
}
