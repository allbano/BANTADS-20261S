// ─── DTOs do ms-conta ────────────────────────────────────────────────

export interface ContaDTO {
  id?: number;
  clienteCpf: string;
  gerenteCpf: string;
  saldo: number;
  limite: number;
  dataCriacao?: string;
}

export interface TransacaoDTO {
  tipo: 'DEPOSITO' | 'SAQUE' | 'TRANSFERENCIA';
  valor: number;
  contaOrigemId?: number;
  contaDestinoId?: number;
  data?: string;
}
