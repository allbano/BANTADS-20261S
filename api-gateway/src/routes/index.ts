import { Router, Request, Response } from 'express';
import httpClient from '../lib/httpClient.js';
import services from '../config/services.js';
import verifyJWT from '../middleware/auth.js';
import requireRole from '../middleware/roleGuard.js';

const router = Router();

// ─── Helpers ────────────────────────────────────────────────────────

function authHeaders(req: Request): Record<string, string> {
  const h: Record<string, string> = {};
  if (req.headers.authorization) h.Authorization = req.headers.authorization;
  return h;
}

/** Encaminha a requisição corrente para {base}{path}, relaiando status e corpo. */
async function forward(req: Request, res: Response, base: string, path: string): Promise<void> {
  try {
    const r = await httpClient.request({
      method: req.method,
      url: base + path,
      data: req.body,
      params: req.query,
      headers: authHeaders(req),
    });
    res.status(r.status).json(r.data);
  } catch (err) {
    res.status(503).json({ error: 'ServiceUnavailable', message: String(err) });
  }
}

// ════════════════════ AUTENTICAÇÃO (R2) ════════════════════
// Público
router.post('/login', (req, res) => forward(req, res, services.auth, '/auth/login'));
router.post('/logout', (req, res) => forward(req, res, services.auth, '/auth/logout'));

// ════════════════════ REBOOT (fan-out a todos os MS) ════════════════════
async function reboot(_req: Request, res: Response): Promise<void> {
  const alvos: Array<[string, string, string]> = [
    ['auth', services.auth, '/auth/reboot'],
    ['cliente', services.cliente, '/reboot'],
    ['conta', services.conta, '/reboot'],
    ['funcionario', services.funcionario, '/reboot'],
  ];
  const resultados = await Promise.allSettled(
    alvos.map(([, base, path]) => httpClient.post(base + path))
  );
  const resumo = alvos.map(([nome], i) => {
    const r = resultados[i];
    return { servico: nome, ok: r.status === 'fulfilled' && r.value.status < 400 };
  });
  res.json({ status: 'reboot', servicos: resumo });
}
router.get('/reboot', reboot);
router.post('/reboot', reboot);

// ════════════════════ CLIENTES ════════════════════
// R1 — Autocadastro (público) → SAGA
router.post('/clientes', (req, res) => forward(req, res, services.saga, '/saga/autocadastro'));

// R9/R12/R14/R16 — listagens (gerente/admin) → ms-cliente (que já compõe com ms-conta)
router.get('/clientes', verifyJWT, (req, res) => forward(req, res, services.cliente, '/clientes'));

// R10 — Aprovar (gerente) → SAGA
router.post('/clientes/:cpf/aprovar', verifyJWT, requireRole('GERENTE', 'ADMIN'),
  (req, res) => forward(req, res, services.saga, `/saga/clientes/${req.params.cpf}/aprovar`));

// R11 — Rejeitar (gerente) → ms-cliente direto (não-SAGA)
router.post('/clientes/:cpf/rejeitar', verifyJWT, requireRole('GERENTE', 'ADMIN'),
  (req, res) => forward(req, res, services.cliente, `/clientes/${req.params.cpf}/rejeitar`));

// R4 — Alteração de perfil → SAGA
router.put('/clientes/:cpf', verifyJWT, (req, res) =>
  forward(req, res, services.saga, `/saga/clientes/${req.params.cpf}`));

// R13 — Detalhe do cliente (API Composition: cliente + conta + gerente)
router.get('/clientes/:cpf', verifyJWT, async (req, res) => {
  try {
    const headers = authHeaders(req);
    const cliRes = await httpClient.get(`${services.cliente}/clientes/por-cpf/${req.params.cpf}`, { headers });
    if (cliRes.status >= 400) { res.status(cliRes.status).json(cliRes.data); return; }
    const cliente = cliRes.data;

    const contaRes = await httpClient.get(`${services.conta}/contas/cliente/${cliente.uuid}`, { headers });
    const conta = contaRes.status < 400 ? contaRes.data : null;

    let gerente = null;
    if (conta?.uuidGerente) {
      const gerRes = await httpClient.get(`${services.funcionario}/gerentes/${conta.uuidGerente}`, { headers });
      if (gerRes.status < 400) gerente = gerRes.data;
    }

    res.json({
      ...cliente,
      conta: conta ? {
        numero: conta.numero, saldo: conta.saldo, limite: conta.limite,
        dataCriacao: conta.dataCriacao, ativo: conta.ativo,
      } : null,
      gerente: gerente ? { nome: gerente.nome, email: gerente.email, cpf: gerente.cpf } : null,
    });
  } catch (err) {
    res.status(503).json({ error: 'ServiceUnavailable', message: String(err) });
  }
});

// ════════════════════ CONTAS (CQRS read + operações) ════════════════════
// Saldo, extrato, movimentações, etc. → ms-conta (JWT). Mapeia a sub-rota direto.
router.use('/contas', verifyJWT, (req, res) => forward(req, res, services.conta, '/contas' + req.url));

// ════════════════════ GERENTES ════════════════════
// R17 — Inserir gerente (admin) → SAGA
router.post('/gerentes', verifyJWT, requireRole('ADMIN'),
  (req, res) => forward(req, res, services.saga, '/saga/gerentes'));

// R19 — Listar gerentes (admin) → ms-funcionario
router.get('/gerentes', verifyJWT, requireRole('ADMIN', 'GERENTE'),
  (req, res) => forward(req, res, services.funcionario, '/gerentes'));

// R18 — Remover gerente (admin) → SAGA
router.delete('/gerentes/:cpf', verifyJWT, requireRole('ADMIN'),
  (req, res) => forward(req, res, services.saga, `/saga/gerentes/${req.params.cpf}`));

// R20 — Alterar gerente (admin) → SAGA
router.put('/gerentes/:cpf', verifyJWT, requireRole('ADMIN'),
  (req, res) => forward(req, res, services.saga, `/saga/gerentes/${req.params.cpf}`));

// Detalhe de gerente (admin/gerente) → ms-funcionario por CPF
router.get('/gerentes/:cpf', verifyJWT, requireRole('ADMIN', 'GERENTE'),
  (req, res) => forward(req, res, services.funcionario, `/gerentes/por-cpf/${req.params.cpf}`));

export default router;
