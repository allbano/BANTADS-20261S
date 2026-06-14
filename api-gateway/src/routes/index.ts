import { Router, Request, Response } from 'express';
import httpClient from '../lib/httpClient.js';
import services from '../config/services.js';
import verifyJWT from '../middleware/auth.js';
import requireRole from '../middleware/roleGuard.js';

const router = Router();

// ─── Helpers ────────────────────────────────────────────────────────
function H(req: Request): Record<string, string> {
  return req.headers.authorization ? { Authorization: req.headers.authorization } : {};
}
async function get(url: string, headers: Record<string, string>) {
  return httpClient.get(url, { headers });
}
/** Encaminha a requisição corrente (proxy puro), relaiando status e corpo. */
async function forward(req: Request, res: Response, base: string, path: string): Promise<void> {
  try {
    const r = await httpClient.request({
      method: req.method, url: base + path, data: req.body, params: req.query, headers: H(req),
    });
    res.status(r.status).json(r.data);
  } catch (err) {
    res.status(503).json({ error: 'ServiceUnavailable', message: String(err) });
  }
}
/** Mapeia GerenteDTO (ms-funcionario) → DadoGerente do contrato (cargo→tipo). */
function dadoGerente(g: any): any {
  if (!g) return null;
  return { cpf: g.cpf, nome: g.nome, email: g.email, tipo: g.tipo ?? g.cargo };
}
async function gerenteCpf(uuid: string | null | undefined, headers: Record<string, string>): Promise<string | null> {
  if (!uuid) return null;
  const g = await get(`${services.funcionario}/gerentes/${uuid}`, headers);
  return g.status < 400 ? g.data.cpf : null;
}

// ════════════════════ AUTH (R2) ════════════════════
router.post('/login', async (req, res) => {
  const r = await httpClient.post(`${services.auth}/auth/login`, req.body);
  if (r.status >= 400) { res.status(r.status).json(r.data); return; }
  const data = r.data;
  const email = data?.usuario?.email;
  let nome = null, cpf = null;
  try {
    if (data.tipo === 'CLIENTE') {
      const c = await get(`${services.cliente}/clientes/por-email/${email}`, H(req));
      if (c.status < 400) { nome = c.data.nome; cpf = c.data.cpf; }
    } else {
      const g = await get(`${services.funcionario}/funcionarios/por-email/${email}`, H(req));
      if (g.status < 400) { nome = g.data.nome; cpf = g.data.cpf; }
    }
  } catch { /* enriquecimento é best-effort */ }
  data.usuario = { nome, cpf, email };
  res.status(200).json(data);
});

router.post('/logout', (req, res) => forward(req, res, services.auth, '/auth/logout'));

// ════════════════════ REBOOT (fan-out) ════════════════════
async function reboot(_req: Request, res: Response): Promise<void> {
  const alvos: Array<[string, string, string]> = [
    ['auth', services.auth, '/auth/reboot'],
    ['cliente', services.cliente, '/reboot'],
    ['conta', services.conta, '/reboot'],
    ['funcionario', services.funcionario, '/reboot'],
  ];
  const resultados = await Promise.allSettled(alvos.map(([, base, path]) => httpClient.post(base + path)));
  const resumo = alvos.map(([nome], i) => {
    const r = resultados[i];
    return { servico: nome, ok: r.status === 'fulfilled' && r.value.status < 400 };
  });
  res.json({ status: 'reboot', servicos: resumo });
}
router.get('/reboot', reboot);

// ════════════════════ CLIENTES ════════════════════
// R1 — Autocadastro (público) → SAGA bloqueante (201 / 409)
router.post('/clientes', (req, res) => forward(req, res, services.saga, '/saga/autocadastro'));

// Listagens (R9/R12/R14/R16)
router.get('/clientes', verifyJWT, async (req, res) => {
  if (req.query.filtro === 'adm_relatorio_clientes') return relatorioClientes(req, res);
  return forward(req, res, services.cliente, '/clientes');
});

// R10 — Aprovar → SAGA; resposta = ContaResponse
router.post('/clientes/:cpf/aprovar', verifyJWT, requireRole('GERENTE', 'ADMIN'), async (req, res) => {
  const r = await httpClient.post(`${services.saga}/saga/clientes/${req.params.cpf}/aprovar`, {});
  if (!r.data?.sucesso) { res.status(400).json({ error: 'aprovacao_falhou', message: r.data?.erro }); return; }
  const uuidCliente = r.data.dados?.uuidCliente;
  const conta = await get(`${services.conta}/contas/cliente/${uuidCliente}`, H(req));
  const c = conta.data ?? {};
  res.status(200).json({
    cliente: c.clienteCpf ?? req.params.cpf, numero: c.numero, saldo: c.saldo,
    limite: c.limite, gerente: await gerenteCpf(c.uuidGerente, H(req)), criacao: c.dataCriacao,
  });
});

// R11 — Rejeitar (direto ms-cliente)
router.post('/clientes/:cpf/rejeitar', verifyJWT, requireRole('GERENTE', 'ADMIN'),
  (req, res) => forward(req, res, services.cliente, `/clientes/${req.params.cpf}/rejeitar`));

// R4 — Alteração de perfil → SAGA
router.put('/clientes/:cpf', verifyJWT, async (req, res) => {
  const r = await httpClient.put(`${services.saga}/saga/clientes/${req.params.cpf}`, req.body);
  if (!r.data?.sucesso) { res.status(400).json({ error: 'alteracao_falhou', message: r.data?.erro }); return; }
  res.status(200).json({ status: 'ok' });
});

// R13 — Detalhe do cliente (API Composition) → DadosClienteResponse (plano)
router.get('/clientes/:cpf', verifyJWT, async (req, res) => {
  const cli = await get(`${services.cliente}/clientes/por-cpf/${req.params.cpf}`, H(req));
  if (cli.status >= 400) { res.status(cli.status).json(cli.data); return; }
  const c = cli.data;
  const conta = await get(`${services.conta}/contas/cliente/${c.uuid}`, H(req));
  const ct = conta.status < 400 ? conta.data : null;
  let gCpf = null, gNome = null, gEmail = null;
  if (ct?.uuidGerente) {
    const g = await get(`${services.funcionario}/gerentes/${ct.uuidGerente}`, H(req));
    if (g.status < 400) { gCpf = g.data.cpf; gNome = g.data.nome; gEmail = g.data.email; }
  }
  res.json({
    cpf: c.cpf, nome: c.nome, telefone: c.telefone ?? null, email: c.email,
    endereco: c.endereco, cidade: c.cidade, estado: c.estado, salario: c.salario,
    conta: ct?.numero ?? null, saldo: ct?.saldo ?? null, limite: ct?.limite ?? null,
    gerente: gCpf, gerente_nome: gNome, gerente_email: gEmail,
  });
});

// R16 — Relatório de clientes (admin): cliente + conta + gerente
async function relatorioClientes(req: Request, res: Response): Promise<void> {
  const headers = H(req);
  const contasRes = await get(`${services.conta}/contas`, headers);
  const contas = contasRes.status < 400 ? contasRes.data : [];
  const result = await Promise.all((contas as any[]).map(async (c) => {
    const cliRes = await get(`${services.cliente}/clientes/por-cpf/${c.clienteCpf}`, headers);
    const cli = cliRes.status < 400 ? cliRes.data : {};
    let gCpf = null, gNome = null, gEmail = null;
    if (c.uuidGerente) {
      const g = await get(`${services.funcionario}/gerentes/${c.uuidGerente}`, headers);
      if (g.status < 400) { gCpf = g.data.cpf; gNome = g.data.nome; gEmail = g.data.email; }
    }
    return {
      cpf: cli.cpf ?? c.clienteCpf, nome: cli.nome, telefone: cli.telefone ?? null, email: cli.email,
      endereco: cli.endereco, cidade: cli.cidade, estado: cli.estado, salario: cli.salario,
      conta: c.numero, saldo: c.saldo, limite: c.limite,
      gerente: gCpf, gerente_nome: gNome, gerente_email: gEmail,
    };
  }));
  result.sort((a, b) => (a.nome || '').localeCompare(b.nome || ''));
  res.json(result);
}

// ════════════════════ CONTAS (passthrough — ms-conta serve o contrato por número) ════════════════════
router.use('/contas', verifyJWT, (req, res) => forward(req, res, services.conta, '/contas' + req.url));

// ════════════════════ GERENTES ════════════════════
// R17 — Inserir → SAGA; resposta = DadoGerente
router.post('/gerentes', verifyJWT, requireRole('ADMIN'), async (req, res) => {
  const r = await httpClient.post(`${services.saga}/saga/gerentes`, req.body);
  if (!r.data?.sucesso) { res.status(400).json({ error: 'insercao_falhou', message: r.data?.erro }); return; }
  res.status(200).json({ cpf: req.body.cpf, nome: req.body.nome, email: req.body.email, tipo: req.body.tipo ?? 'GERENTE' });
});

// R19 / R15 — Listar gerentes ou dashboard
router.get('/gerentes', verifyJWT, requireRole('ADMIN', 'GERENTE'), async (req, res) => {
  if (req.query.numero === 'dashboard') return dashboard(req, res);
  const g = await get(`${services.funcionario}/gerentes`, H(req));
  res.status(g.status).json(Array.isArray(g.data) ? g.data.map(dadoGerente) : g.data);
});

// R18 — Remover → SAGA; resposta = DadoGerente removido
router.delete('/gerentes/:cpf', verifyJWT, requireRole('ADMIN'), async (req, res) => {
  const before = await get(`${services.funcionario}/gerentes/por-cpf/${req.params.cpf}`, H(req));
  const r = await httpClient.delete(`${services.saga}/saga/gerentes/${req.params.cpf}`, { data: {} });
  if (!r.data?.sucesso) { res.status(400).json({ error: 'remocao_falhou', message: r.data?.erro }); return; }
  res.status(200).json(before.status < 400 ? dadoGerente(before.data) : { cpf: req.params.cpf });
});

// R20 — Alterar → SAGA; resposta = DadoGerente
router.put('/gerentes/:cpf', verifyJWT, requireRole('ADMIN'), async (req, res) => {
  const r = await httpClient.put(`${services.saga}/saga/gerentes/${req.params.cpf}`, req.body);
  if (!r.data?.sucesso) { res.status(400).json({ error: 'alteracao_falhou', message: r.data?.erro }); return; }
  const g = await get(`${services.funcionario}/gerentes/por-cpf/${req.params.cpf}`, H(req));
  res.status(200).json(g.status < 400 ? dadoGerente(g.data) : { cpf: req.params.cpf, ...req.body });
});

// Detalhe de gerente
router.get('/gerentes/:cpf', verifyJWT, requireRole('ADMIN', 'GERENTE'), async (req, res) => {
  const g = await get(`${services.funcionario}/gerentes/por-cpf/${req.params.cpf}`, H(req));
  if (g.status >= 400) { res.status(g.status).json(g.data); return; }
  res.json(dadoGerente(g.data));
});

// R15 — Dashboard do gerente/admin: gerente + suas contas + saldos +/−
async function dashboard(req: Request, res: Response): Promise<void> {
  const headers = H(req);
  const gRes = await get(`${services.funcionario}/gerentes`, headers);
  const gerentes = gRes.status < 400 ? gRes.data : [];
  const items = await Promise.all((gerentes as any[]).map(async (g) => {
    const contasRes = await get(`${services.conta}/contas/por-gerente/${g.id}`, headers);
    const contas = contasRes.status < 400 ? contasRes.data : [];
    let pos = 0, neg = 0;
    const clientes = (contas as any[]).map((c) => {
      const s = Number(c.saldo) || 0;
      if (s >= 0) pos += s; else neg += s;
      return { cliente: c.clienteCpf, numero: c.numero, saldo: c.saldo, limite: c.limite, gerente: g.cpf, criacao: c.dataCriacao };
    });
    return { gerente: dadoGerente(g), clientes, saldo_positivo: pos, saldo_negativo: neg };
  }));
  res.json(items);
}

export default router;
