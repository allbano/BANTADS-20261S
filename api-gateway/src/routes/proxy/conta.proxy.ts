import { Router } from 'express';
import services from '../../config/services.js';
import { createProxy } from '../../lib/proxyFactory.js';
import verifyJWT from '../../middleware/auth.js';

const router = Router();

/**
 * @openapi
 * /contas:
 *   get:
 *     tags: [Contas]
 *     summary: Lista todas as contas
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: Lista de contas retornada com sucesso
 *       401:
 *         description: Token não fornecido ou inválido
 *
 * /contas/{id}:
 *   get:
 *     tags: [Contas]
 *     summary: Busca conta por ID
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: integer
 *     responses:
 *       200:
 *         description: Dados da conta
 *       404:
 *         description: Conta não encontrada
 */

const init = async () => {
  router.use('/', verifyJWT, await createProxy(services.conta, '/contas'));
};
init();

export default router;
