import { Injectable, inject } from '@angular/core';

import type { PerfilCliente, AlterarSenhaPayload } from '../../domain/models/perfil-cliente.model';
import type { ResultadoOperacao } from '../../domain/models/resultado-operacao.model';
import { PerfilClienteRepository } from '../../domain/repositories/perfil-cliente.repository';
import { CLIENTES_DEMONSTRACAO } from '../data/clientes-demonstracao';
import { ClienteLocalStorageService } from './cliente-local-storage.service';

const ENDERECO_PADRAO = {
  cep: '',
  logradouro: '',
  numero: '',
  complemento: '',
  cidade: '',
  uf: '',
};

/**
 * Implementação concreta do PerfilClienteRepository usando o mock em memória/localStorage.
 *
 * Estratégia de leitura (mesclagem):
 *   1. Parte do seed (CLIENTES_DEMONSTRACAO) como base.
 *   2. Sobrescreve com dados gravados pelo usuário no localStorage (ClienteLocalStorageService).
 *
 * Assim clientes novos do autocadastro também aparecem corretamente.
 */
@Injectable({
  providedIn: 'root',
})
export class PerfilClienteMockService extends PerfilClienteRepository {
  private readonly armazenamento = inject(ClienteLocalStorageService);

  override buscarPerfil(clienteId: number): PerfilCliente | null {
    // Monta mapa: seed → depois sobrescreve com dados do localStorage
    const map = new Map(CLIENTES_DEMONSTRACAO.map(c => [c.id, { ...c }]));
    for (const c of this.armazenamento.listarTodos()) {
      map.set(c.id, { ...c });
    }

    const cliente = map.get(clienteId);
    if (!cliente) {
      return null;
    }

    return {
      clienteId: cliente.id,
      nome: cliente.nome,
      cpf: cliente.cpf,
      email: cliente.email,
      salario: cliente.salario,
      telefone: cliente.telefone ?? '',
      endereco: cliente.endereco ?? { ...ENDERECO_PADRAO },
    };
  }

  override salvarPerfil(perfil: PerfilCliente): ResultadoOperacao {
    // Carrega o registro completo existente (para manter a senha intacta)
    const map = new Map(CLIENTES_DEMONSTRACAO.map(c => [c.id, { ...c }]));
    for (const c of this.armazenamento.listarTodos()) {
      map.set(c.id, { ...c });
    }

    const existente = map.get(perfil.clienteId);
    if (!existente) {
      return { sucesso: false, mensagem: 'Cliente não encontrado.' };
    }

    const atualizado = {
      ...existente,
      nome: perfil.nome.trim(),
      email: perfil.email.trim(),
      telefone: perfil.telefone.trim(),
      salario: perfil.salario,
      endereco: { ...perfil.endereco },
    };

    this.armazenamento.atualizar(atualizado);
    return { sucesso: true, mensagem: 'Perfil atualizado com sucesso!' };
  }

  override alterarSenha(payload: AlterarSenhaPayload): ResultadoOperacao {
    const map = new Map(CLIENTES_DEMONSTRACAO.map(c => [c.id, { ...c }]));
    for (const c of this.armazenamento.listarTodos()) {
      map.set(c.id, { ...c });
    }

    const existente = map.get(payload.clienteId);
    if (!existente) {
      return { sucesso: false, mensagem: 'Cliente não encontrado.' };
    }

    if (existente.senha !== payload.senhaAtual) {
      return { sucesso: false, mensagem: 'Senha atual incorreta.' };
    }

    if (payload.novaSenha.trim().length < 4) {
      return { sucesso: false, mensagem: 'A nova senha deve ter pelo menos 4 caracteres.' };
    }

    const atualizado = { ...existente, senha: payload.novaSenha };
    this.armazenamento.atualizar(atualizado);
    return { sucesso: true, mensagem: 'Senha alterada com sucesso!' };
  }
}
