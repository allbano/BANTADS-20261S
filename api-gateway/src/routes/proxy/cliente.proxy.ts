import { Router } from 'express';
import services from '../../config/services.js';
import { createProxy } from '../../lib/proxyFactory.js';
import verifyJWT from '../../middleware/auth.js';

const router = Router();

/**
 * @openapi
 * /clientes:
 *   get:
 *     tags: [Clientes]
 *     summary: Lista todos os clientes
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: Lista de clientes retornada com sucesso
 *       401:
 *         description: Token não fornecido ou inválido
 *
 * /clientes/{id}:
 *   get:
 *     tags: [Clientes]
 *     summary: Busca cliente por ID
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
 *         description: Dados do cliente
 *       404:
 *         description: Cliente não encontrado
 */

const init = async () => {
  router.use('/', verifyJWT, await createProxy(services.cliente, '/clientes'));
};
init();

export default router;
