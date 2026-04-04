/**
 * Resultado de uma ação de aprovação ou rejeição de pedido (R10/R11).
 */
export type ResultadoAprovacao =
  | { sucesso: true; mensagem: string }
  | { sucesso: false; mensagem: string };
