import { Injectable } from '@angular/core';
import { ClienteRepository } from '../../domain/repositories/cliente.repository';
import { Cliente } from '../../domain/models/cliente.model';

const LS_CHAVE = 'clientes';

/**
 * Implementação concreta do ClienteRepository usando localStorage.
 *
 * Segue o mesmo padrão de AutocadastroApiService — a camada de infraestrutura
 * implementa o contrato definido pelo domínio.
 */
@Injectable({
  providedIn: 'root',
})
export class ClienteLocalStorageService extends ClienteRepository {

  listarTodos(): Cliente[] {
    const dados = localStorage.getItem(LS_CHAVE);
    return dados ? JSON.parse(dados) : [];
  }

  buscarPorCPF(cpf: number): Cliente | undefined {
    return this.listarTodos().find(cliente => cliente.cpf === cpf);
  }

  inserir(cliente: Cliente): void {
    const clientes = this.listarTodos();

    const novoCliente: Cliente = {
      ...cliente,
      id: Date.now(),
    };

    clientes.push(novoCliente);
    localStorage.setItem(LS_CHAVE, JSON.stringify(clientes));
  }

  atualizar(cliente: Cliente): void {
    const clientes = this.listarTodos();
    const idx = clientes.findIndex(c => c.id === cliente.id);
    if (idx !== -1) {
      clientes[idx] = cliente;
    } else {
      // Cliente ainda não está no localStorage (ex: clientes do seed de demonstração)
      clientes.push(cliente);
    }
    localStorage.setItem(LS_CHAVE, JSON.stringify(clientes));
  }

  remover(id: number): void {
    const clientes = this.listarTodos().filter(c => c.id !== id);
    localStorage.setItem(LS_CHAVE, JSON.stringify(clientes));
  }
}
