import { Request, Response, NextFunction } from 'express';
import { AuthorizationError } from '../types/errors.js';
import type { AuthenticatedUser } from '../types/express.js';

/**
 * Middleware de autorização por role.
 * Deve ser usado APÓS o verifyJWT (que popula req.user).
 *
 * Padrão de projeto: Strategy — a "estratégia" de autorização muda
 * conforme os roles permitidos passados como argumento.
 *
 * @param allowedRoles - Roles que têm acesso à rota
 * @returns Middleware Express
 *
 * @example
 * router.get('/admin', verifyJWT, requireRole('ADMIN'), handler);
 * router.get('/dashboard', verifyJWT, requireRole('CLIENTE', 'GERENTE'), handler);
 */
const requireRole = (...allowedRoles: AuthenticatedUser['tipo'][]) => {
  return (req: Request, res: Response, next: NextFunction): void => {
    if (!req.user) {
      res.status(401).json({
        auth: false,
        message: 'Usuário não autenticado.',
      });
      return;
    }

    if (!allowedRoles.includes(req.user.tipo)) {
      res.status(403).json({
        error: 'AuthorizationError',
        message: `Acesso restrito aos perfis: ${allowedRoles.join(', ')}.`,
      });
      return;
    }

    next();
  };
};

export default requireRole;
