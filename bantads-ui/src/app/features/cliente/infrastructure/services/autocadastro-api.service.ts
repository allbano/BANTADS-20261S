import { Injectable, inject } from '@angular/core';
import { Observable, timer, throwError } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';

import { AutocadastroRepository } from '../../domain/repositories/autocadastro.repository';
import { AutocadastroPayload } from '../../domain/models/autocadastro.model';

import { ClienteLocalStorageService } from './cliente-local-storage.service';
import { CLIENTES_DEMONSTRACAO } from '../data/clientes-demonstracao';
import { AprovacaoMockService } from '../../../gerente/infrastructure/services/aprovacao-mock.service';
import { GerenteAdminMockService } from '../../../administrador/infrastructure/services/gerente-admin-mock.service';
import { DashboardAdminMockService } from '../../../administrador/infrastructure/services/dashboard-admin-mock.service';
import type { PedidoAutocadastro } from '../../../gerente/domain/models/pedido-autocadastro.model';

@Injectable({
  providedIn: 'root'
})
export class AutocadastroApiService implements AutocadastroRepository {
  private readonly clienteStorage = inject(ClienteLocalStorageService);
  private readonly aprovacaoMock = inject(AprovacaoMockService);
  private readonly gerenteAdminMock = inject(GerenteAdminMockService);
  private readonly dashboardAdminMock = inject(DashboardAdminMockService);

  solicitarCadastro(payload: AutocadastroPayload): Observable<void> {
    console.log('[API] Verificando solicitação de autocadastro...', payload);
    
    // Normaliza CPF
    const cpfNum = Number(payload.dadosPessoais.cpf.replace(/\D/g, ''));
    const cpfStr = String(cpfNum);

    // R1.5 - Verifica se já é cliente aprovado/ativo
    const jaAprovadoStorage = this.clienteStorage.listarTodos().some(c => c.cpf === cpfNum);
    const jaAprovadoDemo = CLIENTES_DEMONSTRACAO.some(c => c.cpf === cpfNum);
    
    // R1.5 - Verifica se já possui pedido pendente c/ o mesmo CPF
    const jaPendente = this.aprovacaoMock.listarPendentes().some(p => p.cpf.replace(/\D/g, '') === cpfStr);

    if (jaAprovadoStorage || jaAprovadoDemo || jaPendente) {
      console.warn('[API] Falha no Autocadastro: CPF já cadastrado ou aguardando aprovação.');
      
      // R1.7 - E-mail enviado em caso de falha (simulação)
      console.log(
        `📧 [MOCK E-MAIL] Para: ${payload.dadosPessoais.email}\n` +
        `   Assunto: Erro na solicitação do BANTADS\n` +
        `   Corpo: Olá ${payload.dadosPessoais.nome}, identificamos que o CPF informado já está registrado em nossa base de dados.`
      );
      
      return timer(500).pipe(
        switchMap(() => throwError(() => new Error('Este CPF já está cadastrado ou em processo de aprovação.')))
      );
    }

    return timer(1500).pipe(
      map(() => {
        // R1.3 - Definir gerente com menos clientes atribuído automaticamente pelo sistema
        const estatisticas = this.dashboardAdminMock.obterEstatisticas();
        
        let gerenteMenosClientesId: number | null = null;
        let minClientes = Infinity;

        for (const stat of estatisticas) {
          if (stat.qtdClientes < minClientes) {
            minClientes = stat.qtdClientes;
            gerenteMenosClientesId = stat.gerenteId;
          }
        }

        // Falback se o mock dashboard falhar
        if (!gerenteMenosClientesId) {
          const gerentes = this.gerenteAdminMock.listarTodos();
          if (gerentes.length > 0) {
            gerenteMenosClientesId = gerentes[0].id;
          } else {
            throw new Error('Erro de sistema: Nenhum gerente disponível.');
          }
        }

        // R1.2 / R1.6 - Salva o pedido para aprovação futura, informando o gerente localmente
        const novoPedido: PedidoAutocadastro = {
          id: Date.now(),
          nome: payload.dadosPessoais.nome,
          cpf: String(cpfNum).padStart(11, '0'),
          email: payload.dadosPessoais.email,
          telefone: payload.dadosPessoais.telefone,
          salario: payload.dadosPessoais.salario,
          endereco: { ...payload.endereco },
          gerenteId: gerenteMenosClientesId,
          dataSolicitacao: new Date().toISOString()
        };

        this.aprovacaoMock.registrarPedido(novoPedido);
        console.log(`[API] Pedido registrado e atribuído ao Gerente ID ${gerenteMenosClientesId}.`);
      })
    );
  }
}
