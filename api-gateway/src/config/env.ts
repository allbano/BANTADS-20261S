import 'dotenv/config';

/**
 * Configuração centralizada de variáveis de ambiente.
 * Todas as variáveis devem ser lidas SOMENTE aqui e exportadas tipadas.
 */

export const PORT = Number(process.env.PORT) || 3000;
export const JWT_SECRET = process.env.JWT_SECRET ?? 'change-me-in-production';

export const SERVICES = {
  AUTH:         process.env.MS_AUTH_URL         ?? 'http://localhost:8085',
  CLIENTE:      process.env.MS_CLIENTE_URL      ?? 'http://localhost:8081',
  CONTA:        process.env.MS_CONTA_URL        ?? 'http://localhost:8082',
  FUNCIONARIO:  process.env.MS_FUNCIONARIO_URL  ?? 'http://localhost:8083',
} as const;
