export type ResultadoOperacao =
  | { sucesso: true; mensagem: string }
  | { sucesso: false; mensagem: string };
