/**
 * Resumo de cliente para o ranking de maiores saldos (R14).
 * Apenas os campos necessários para a tabela de top 3.
 */
export interface TopCliente {
  cpf: string;
  nome: string;
  cidade: string;
  estado: string;
  saldo: number;
}
