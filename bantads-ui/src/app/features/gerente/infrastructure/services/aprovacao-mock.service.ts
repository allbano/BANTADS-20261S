import { Injectable } from '@angular/core';

import type { PedidoAutocadastro } from '../../domain/models/pedido-autocadastro.model';
import type { ResultadoAprovacao } from '../../domain/models/resultado-aprovacao.model';
import { AprovacaoRepository } from '../../domain/repositories/aprovacao.repository';
import { PEDIDOS_AUTOCADASTRO_MOCK } from '../data/pedidos-autocadastro-mock';
import { CLIENTES_GERENTE_MOCK } from '../data/clientes-gerente-mock';
import type { ClienteGerente } from '../../domain/models/cliente-gerente.model';

function gerarNumeroConta(): string {
  return String(Math.floor(1000 + Math.random() * 9000));
}

function gerarSenhaAleatoria(): string {
  const chars = 'ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789';
  let senha = '';
  for (let i = 0; i < 8; i++) {
    senha += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return senha;
}

function calcularLimite(salario: number): number {
  return salario >= 2000 ? salario / 2 : 0;
}

function agoraIso(): string {
  return new Date().toISOString();
}

/**
 * Implementação mock do AprovacaoRepository (R9/R10/R11).
 *
 * Mantém os pedidos em memória. Aprovação cria uma conta e
 * simula envio de e-mail via console.log.
 */
@Injectable({ providedIn: 'root' })
export class AprovacaoMockService extends AprovacaoRepository {
  private pedidos: PedidoAutocadastro[] = [...PEDIDOS_AUTOCADASTRO_MOCK];

  override listarPendentes(gerenteId?: number): PedidoAutocadastro[] {
    if (gerenteId !== undefined) {
      return this.pedidos.filter(p => p.gerenteId === gerenteId);
    }
    return [...this.pedidos];
  }

  override registrarPedido(pedido: PedidoAutocadastro): void {
    this.pedidos.push(pedido);
  }

  /**
   * R10 — Aprovar cliente:
   * 1. Gera número de conta aleatório (4 dígitos)
   * 2. Calcula limite (salário ≥ R$2.000 → salário / 2; senão 0)
   * 3. Gera senha aleatória
   * 4. Cria registro no array de clientes do gerente
   * 5. Remove o pedido da lista de pendentes
   * 6. "Envia" e-mail com a senha (console.log)
   */
  override aprovar(pedidoId: number): ResultadoAprovacao {
    const idx = this.pedidos.findIndex(p => p.id === pedidoId);
    if (idx === -1) {
      return { sucesso: false, mensagem: 'Pedido não encontrado.' };
    }

    const pedido = this.pedidos[idx];
    const numeroConta = gerarNumeroConta();
    const limite = calcularLimite(pedido.salario);
    const senha = gerarSenhaAleatoria();

    // Cria o registro de cliente aprovado
    const novoCliente: ClienteGerente = {
      id: Date.now(),
      nome: pedido.nome,
      cpf: pedido.cpf,
      email: pedido.email,
      telefone: pedido.telefone,
      salario: pedido.salario,
      endereco: { ...pedido.endereco },
      numeroConta,
      saldo: 0,
      limite,
      dataAberturaConta: agoraIso().slice(0, 10),
    };

    // Adiciona ao array global de clientes do gerente
    CLIENTES_GERENTE_MOCK.push(novoCliente);

    // Remove da lista de pendentes
    this.pedidos.splice(idx, 1);

    // Simula envio de e-mail com senha
    console.log(
      `📧 [MOCK E-MAIL] Para: ${pedido.email}\n` +
      `   Assunto: Sua conta BANTADS foi aprovada!\n` +
      `   Corpo: Olá ${pedido.nome}, sua conta nº ${numeroConta} foi criada.\n` +
      `   Sua senha de acesso: ${senha}\n` +
      `   Limite: R$ ${limite.toFixed(2)}\n` +
      `   Data/hora aprovação: ${agoraIso()}`
    );

    return {
      sucesso: true,
      mensagem: `Cliente "${pedido.nome}" aprovado! Conta ${numeroConta} criada. E-mail enviado para ${pedido.email}.`,
    };
  }

  /**
   * R11 — Rejeitar cliente:
   * 1. Registra o motivo e a data/hora
   * 2. Remove o pedido da lista de pendentes
   * 3. "Envia" e-mail com o motivo da reprovação
   */
  override rejeitar(pedidoId: number, motivo: string): ResultadoAprovacao {
    const idx = this.pedidos.findIndex(p => p.id === pedidoId);
    if (idx === -1) {
      return { sucesso: false, mensagem: 'Pedido não encontrado.' };
    }

    if (!motivo.trim()) {
      return { sucesso: false, mensagem: 'O motivo da rejeição é obrigatório.' };
    }

    const pedido = this.pedidos[idx];

    // Simula envio de e-mail com motivo
    console.log(
      `📧 [MOCK E-MAIL] Para: ${pedido.email}\n` +
      `   Assunto: Sua solicitação BANTADS foi recusada\n` +
      `   Corpo: Olá ${pedido.nome}, infelizmente sua solicitação de abertura de conta foi recusada.\n` +
      `   Motivo: ${motivo}\n` +
      `   Data/hora rejeição: ${agoraIso()}`
    );

    // Remove da lista de pendentes
    this.pedidos.splice(idx, 1);

    return {
      sucesso: true,
      mensagem: `Pedido de "${pedido.nome}" recusado. E-mail enviado para ${pedido.email}.`,
    };
  }
}
