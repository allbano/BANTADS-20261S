import { Router, Request, Response, NextFunction } from 'express';
import verifyJWT from '../../middleware/auth.js';
import requireRole from '../../middleware/roleGuard.js';
import { GerenteService } from '../../services/GerenteService.js';

const router = Router();
const gerenteService = new GerenteService();

/**
 * @openapi
 * /gerentes:
 *   get:
 *     tags: [Gerentes]
 *     summary: Lista gerentes com seus clientes vinculados (API Composition)
 *     description: |
 *       Rota de **API Composition**: o gateway consulta ms-funcionario e ms-conta
 *       em paralelo, cruza os dados por CPF do gerente, e retorna a lista composta.
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: Lista de gerentes com clientes
 *         content:
 *           application/json:
 *             schema:
 *               type: array
 *               items:
 *                 type: object
 *                 properties:
 *                   nome:
 *                     type: string
 *                   cpf:
 *                     type: string
 *                   clientes:
 *                     type: array
 *                     items:
 *                       type: object
 *       401:
 *         description: Token não fornecido ou inválido
 *       403:
 *         description: Acesso restrito a administradores
 *       503:
 *         description: Um dos microsserviços está indisponível
 */
router.get(
  '/',
  verifyJWT,
  async (req: Request, res: Response, next: NextFunction) => {
    try {
      const result = await gerenteService.listarComClientes(
        req.headers.authorization!
      );
      res.json(result);
    } catch (error) {
      next(error);
    }
  }
);

export default router;
