import { SERVICES } from './env.js';

/**
 * Registry de URLs dos microsserviços.
 * Mapeamento semântico para uso nas rotas e services.
 */
const services = {
  auth:         SERVICES.AUTH,
  cliente:      SERVICES.CLIENTE,
  conta:        SERVICES.CONTA,
  funcionario:  SERVICES.FUNCIONARIO,
  saga:         SERVICES.SAGA,
} as const;

export default services;
