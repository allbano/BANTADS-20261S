import { Request, Response, NextFunction, ErrorRequestHandler } from 'express';
import { GatewayError } from '../types/errors.js';

/**
 * Middleware global de tratamento de erros.
 * Captura todos os erros lançados pelas rotas e retorna uma resposta padronizada.
 *
 * Padrão de projeto: Chain of Responsibility — esse é o último elo da cadeia.
 */
const errorHandler: ErrorRequestHandler = (
  err: Error,
  _req: Request,
  res: Response,
  _next: NextFunction
): void => {
  // Erros conhecidos do gateway
  if (err instanceof GatewayError) {
    res.status(err.statusCode).json({
      error: err.name,
      message: err.message,
      ...(err.details ? { details: err.details } : {}),
    });
    return;
  }

  // Erros inesperados
  console.error('[UNHANDLED ERROR]', err);
  res.status(500).json({
    error: 'InternalServerError',
    message: 'Erro interno do gateway.',
  });
};

export default errorHandler;
