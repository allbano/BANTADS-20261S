import { Router } from 'express';
import services from '../../config/services.js';
import { createProxy } from '../../lib/proxyFactory.js';

const router = Router();

/**
 * @openapi
 * /auth/login:
 *   post:
 *     tags: [Auth]
 *     summary: Realiza login do usuário
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required: [login, senha]
 *             properties:
 *               login:
 *                 type: string
 *                 example: "admin@bantads.com"
 *               senha:
 *                 type: string
 *                 example: "ABC123"
 *     responses:
 *       200:
 *         description: Token JWT retornado com sucesso
 *       401:
 *         description: Credenciais inválidas
 *
 * /auth/reboot:
 *   get:
 *     tags: [Auth]
 *     summary: Reinicializa o banco de dados do ms-auth
 *     responses:
 *       200:
 *         description: Banco de dados reinicializado
 */

// Inicialização assíncrona para proxy ESM
const init = async () => {
  router.use('/', await createProxy(services.auth, '/auth'));
};
init();

export default router;
