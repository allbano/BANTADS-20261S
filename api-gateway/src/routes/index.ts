import { Router } from 'express';

// ─── Proxy routes (1 rota → 1 microserviço) ────────────────────────
import authProxy from './proxy/auth.proxy.js';
import clienteProxy from './proxy/cliente.proxy.js';
import contaProxy from './proxy/conta.proxy.js';

// ─── Composition routes (1 rota → N microserviços) ─────────────────
import gerenteComposition from './composition/gerente.composition.js';

const router = Router();

// Proxy Routes
router.use('/auth', authProxy);
router.use('/clientes', clienteProxy);
router.use('/contas', contaProxy);

// API Composition Routes
router.use('/gerentes', gerenteComposition);

export default router;
