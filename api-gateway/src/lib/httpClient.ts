import axios from 'axios';

/**
 * Cliente HTTP único do gateway (axios). `validateStatus` sempre verdadeiro:
 * o gateway relaia o status do microsserviço tal como recebido, sem lançar
 * exceção para 4xx/5xx — só erros de rede caem no catch (→ 503).
 */
const httpClient = axios.create({
  // Maior que o timeout das SAGAs bloqueantes no ms-saga (20s), para receber
  // o status final (200/201/409/400) em vez de estourar antes (503).
  timeout: 30000,
  validateStatus: () => true,
});

export default httpClient;
