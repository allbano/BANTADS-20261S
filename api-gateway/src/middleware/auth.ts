import { Request, Response, NextFunction } from 'express';
import jwt from 'jsonwebtoken';
import { JWT_SECRET } from '../config/env.js';
import { AuthenticationError } from '../types/errors.js';
import type { AuthenticatedUser } from '../types/express.js';

/**
 * Payload esperado dentro do token JWT gerado pelo ms-auth.
 */
interface JwtPayload {
  id: string;
  tipo: AuthenticatedUser['tipo'];
  login: string;
  iat?: number;
  exp?: number;
}

/**
 * Middleware de verificação de token JWT.
 * Valida a identidade do usuário antes de permitir que o API Gateway
 * repasse a requisição para os microsserviços internos.
 */
const verifyJWT = (req: Request, res: Response, next: NextFunction): void => {
  const authHeader = req.headers['authorization'];

  if (!authHeader) {
    res.status(401).json({
      auth: false,
      message: 'Acesso negado. Token não fornecido.',
    });
    return;
  }

  // Deve seguir o padrão "Bearer <TOKEN>"
  const parts = authHeader.split(' ');
  if (parts.length !== 2 || parts[0] !== 'Bearer') {
    res.status(401).json({
      auth: false,
      message: 'Erro no formato do token.',
    });
    return;
  }

  const token = parts[1];

  try {
    const decoded = jwt.verify(token, JWT_SECRET) as JwtPayload;

    req.user = {
      id: decoded.id,
      tipo: decoded.tipo,
      login: decoded.login,
    };

    next();
  } catch {
    res.status(401).json({
      auth: false,
      message: 'Token inválido ou expirado.',
    });
  }
};

export default verifyJWT;
