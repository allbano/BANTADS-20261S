/**
 * Agregado de leitura para o dashboard do administrador (R15).
 *
 * Para cada gerente, apresenta quantos clientes possui,
 * a totalização de saldos positivos e negativos.
 */
export interface GerenteDashboard {
  gerenteId: number;
  nome: string;
  qtdClientes: number;
  /** Soma de todos os saldos >= 0 dos clientes deste gerente. */
  saldoPositivo: number;
  /** Soma de todos os saldos < 0 dos clientes deste gerente (valor negativo). */
  saldoNegativo: number;
}
