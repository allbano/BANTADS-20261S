import httpClient from '../lib/httpClient.js';
import services from '../config/services.js';
import type { FuncionarioDTO, GerenteComClientesDTO } from '../types/dto/funcionario.dto.js';
import type { ContaDTO } from '../types/dto/conta.dto.js';

/**
 * Service de composição para dados de gerentes.
 *
 * Padrão de projeto: Facade — esconde a complexidade de orquestrar
 * chamadas a múltiplos microsserviços (ms-funcionario + ms-conta)
 * e montar a resposta composta.
 */
export class GerenteService {

  /**
   * Lista todos os gerentes com as contas/clientes que cada um atende.
   * Realiza chamadas paralelas a ms-funcionario e ms-conta,
   * depois cruza os dados por CPF do gerente.
   *
   * @param authHeader - Header Authorization para repassar aos MSs
   * @returns Lista de gerentes com seus clientes vinculados
   */
  async listarComClientes(authHeader: string): Promise<GerenteComClientesDTO[]> {
    const headers = { Authorization: authHeader };

    const [funcionariosResponse, contasResponse] = await Promise.all([
      httpClient.get<FuncionarioDTO[]>(`${services.funcionario}/gerentes/`, { headers }),
      httpClient.get<ContaDTO[]>(`${services.conta}/contas/`, { headers }),
    ]);

    // Agrupa contas por CPF do gerente para lookup O(1) por gerente
    const contasPorGerente = contasResponse.data.reduce<Record<string, ContaDTO[]>>(
      (acc, conta) => {
        const cpf = conta.gerenteCpf;
        if (!acc[cpf]) acc[cpf] = [];
        acc[cpf].push(conta);
        return acc;
      },
      {}
    );

    // Monta o DTO composto
    return funcionariosResponse.data.map((funcionario) => ({
      ...funcionario,
      clientes: contasPorGerente[funcionario.cpf] || [],
    }));
  }
}
