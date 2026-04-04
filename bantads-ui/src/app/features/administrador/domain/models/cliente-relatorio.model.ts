/**
 * Modelo de leitura para o Relatório de Clientes (R16).
 *
 * Contém todos os dados que devem ser exibidos na tabela do relatório:
 * CPF, Nome, E-mail, Salário, Número da conta, Saldo, Limite,
 * CPF do gerente e Nome do gerente.
 */
export interface ClienteRelatorio {
  cpf: string;
  nome: string;
  email: string;
  salario: number;
  numeroConta: string;
  saldo: number;
  limite: number;
  cpfGerente: string;
  nomeGerente: string;
}
