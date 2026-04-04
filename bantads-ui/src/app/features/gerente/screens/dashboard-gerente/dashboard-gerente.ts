import { Component, inject, OnInit, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';

import { DashboardGerenteFacade } from '../../application/facades/dashboard-gerente.facade';
import { AprovacaoRepository } from '../../domain/repositories/aprovacao.repository';
import { AprovacaoMockService } from '../../infrastructure/services/aprovacao-mock.service';
import { GerenteTopNav } from '../../components/gerente-top-nav/gerente-top-nav';
import { ModalAprovarRejeitar } from '../../components/modal-aprovar-rejeitar/modal-aprovar-rejeitar';
import { ModalRejeitarMotivo } from '../../components/modal-rejeitar-motivo/modal-rejeitar-motivo';
import type { PedidoAutocadastro } from '../../domain/models/pedido-autocadastro.model';

@Component({
  selector: 'app-dashboard-gerente',
  imports: [CurrencyPipe, GerenteTopNav, ModalAprovarRejeitar, ModalRejeitarMotivo],
  templateUrl: './dashboard-gerente.html',
  providers: [
    DashboardGerenteFacade,
    { provide: AprovacaoRepository, useExisting: AprovacaoMockService },
  ],
})
export class DashboardGerente implements OnInit {
  readonly facade = inject(DashboardGerenteFacade);

  readonly pedidoSelecionado = signal<PedidoAutocadastro | null>(null);
  readonly modalDetalhesAberto = signal(false);
  readonly modalMotivoAberto = signal(false);
  readonly pedidoParaRejeitar = signal<number | null>(null);

  ngOnInit(): void {
    this.facade.carregar();
  }

  abrirDetalhes(pedido: PedidoAutocadastro): void {
    this.pedidoSelecionado.set(pedido);
    this.modalDetalhesAberto.set(true);
  }

  fecharDetalhes(): void {
    this.modalDetalhesAberto.set(false);
    this.pedidoSelecionado.set(null);
  }

  aprovarPedido(pedidoId: number): void {
    this.fecharDetalhes();
    this.facade.aprovar(pedidoId);
  }

  iniciarRejeicao(pedidoId: number): void {
    this.fecharDetalhes();
    this.pedidoParaRejeitar.set(pedidoId);
    this.modalMotivoAberto.set(true);
  }

  confirmarRejeicao(motivo: string): void {
    const pedidoId = this.pedidoParaRejeitar();
    if (pedidoId !== null) {
      this.facade.rejeitar(pedidoId, motivo);
    }
    this.fecharModalMotivo();
  }

  fecharModalMotivo(): void {
    this.modalMotivoAberto.set(false);
    this.pedidoParaRejeitar.set(null);
  }
}
