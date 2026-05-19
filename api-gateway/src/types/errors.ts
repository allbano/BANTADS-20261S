/**
 * Classes de erro customizadas para o API Gateway.
 * Permitem tratamento centralizado no errorHandler middleware.
 */

/** Erro genérico do gateway, com status HTTP e detalhes opcionais */
export class GatewayError extends Error {
  public readonly statusCode: number;
  public readonly details?: unknown;

  constructor(message: string, statusCode: number = 502, details?: unknown) {
    super(message);
    this.name = 'GatewayError';
    this.statusCode = statusCode;
    this.details = details;
  }
}

/** Erro de autenticação (401) */
export class AuthenticationError extends GatewayError {
  constructor(message: string = 'Acesso negado. Token não fornecido ou inválido.') {
    super(message, 401);
    this.name = 'AuthenticationError';
  }
}

/** Erro de autorização (403) */
export class AuthorizationError extends GatewayError {
  constructor(message: string = 'Você não tem permissão para acessar este recurso.') {
    super(message, 403);
    this.name = 'AuthorizationError';
  }
}

/** Erro quando um microsserviço downstream está indisponível */
export class ServiceUnavailableError extends GatewayError {
  constructor(serviceName: string) {
    super(`Serviço '${serviceName}' está indisponível.`, 503);
    this.name = 'ServiceUnavailableError';
  }
}
