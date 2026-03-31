import { Injectable } from '@angular/core';
import { Cliente } from '../shared/models/cliente.model'


const LS_CHAVE = "clientes";

@Injectable({
  providedIn: 'root',
})
export class ClienteService {

  listarTodos(): Cliente[] {
    const clientes = localStorage[LS_CHAVE];
    return clientes ? JSON.parse(clientes) : [];
  }

  buscarPorCPF(cpf: number): Cliente | undefined {
    const clientes = this.listarTodos();

    return clientes.find(cliente => cliente.cpf === cpf);
  }

  inserir(cliente: Cliente): void {
    
    const clientes = this.listarTodos();
    
    cliente.id = new Date().getTime();
    
    clientes.push(cliente);

    // Armazena no LocalStorage
    localStorage[LS_CHAVE] = JSON.stringify(cliente);
  }

  atualizar(cliente: Cliente): void {
    
    const clientes = this.listarTodos();
    
    // Quando encontra pessoa com mesmo id, altera a lista
    clientes.forEach( (obj, index, objs) => {
      if (cliente.id === obj.id) {
        objs[index] = cliente
      }
    });

    localStorage[LS_CHAVE] = JSON.stringify(clientes);

  }

  remover(id: number): void {
    let clientes = this.listarTodos();
    
    clientes = clientes.filter(cliente => cliente.id !== id);
    localStorage[LS_CHAVE] = JSON.stringify(clientes);
  }


}
