import { Request, Response, NextFunction } from 'express';
import jwt from 'jsonwebtoken';
import { JWT_SECRET } from '../config/env.js';
import type { AuthenticatedUser } from '../types/express.js';
import { isRevoked } from '../lib/revokedTokens.js';

/**
 * Claims do token JWT gerado pelo ms-auth (auth0-jwt):
 * subject = e-mail; claim "email"; claim "role" em minúsculo
 * (cliente | gerente | administrador).
 */
interface JwtPayload {
  sub?: string;
  email?: string;
  role?: string;
  iat?: number;
  exp?: number;
}

/** Mapeia o "role" do ms-auth (minúsculo) para o tipo usado no gateway. */
function mapTipo(role?: string): AuthenticatedUser['tipo'] {
  switch ((role ?? '').toLowerCase()) {
    case 'gerente': return 'GERENTE';
    case 'administrador':
    case 'admin': return 'ADMIN';
    default: return 'CLIENTE';
  }
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

  // Token revogado por logout (R2) → tratado como inválido.
  if (isRevoked(token)) {
    res.status(401).json({ auth: false, message: 'Token inválido ou expirado.' });
    return;
  }

  try {
    const decoded = jwt.verify(token, JWT_SECRET) as JwtPayload;

    const login = decoded.email ?? decoded.sub ?? '';
    req.user = {
      id: login,
      tipo: mapTipo(decoded.role),
      login,
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
