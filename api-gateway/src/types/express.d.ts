/**
 * Extensão da interface Request do Express para incluir dados do usuário autenticado.
 * Esse módulo usa "declaration merging" do TypeScript para estender o namespace do Express.
 */

export interface AuthenticatedUser {
  id: string;
  tipo: 'CLIENTE' | 'GERENTE' | 'ADMIN';
  login: string;
}

declare global {
  namespace Express {
    interface Request {
      user?: AuthenticatedUser;
    }
  }
}
