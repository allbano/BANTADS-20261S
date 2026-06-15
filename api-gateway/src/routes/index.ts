/**
 * Roteador único do API Gateway (Richardson nível 2) — o ÚNICO ponto de acesso do
 * front-end (porta 3000). Traduz a API REST pública (contrato test_dac) para os
 * microsserviços internos e aplica os padrões:
 *  - API Gateway: expõe todas as rotas; verifica JWT (verifyJWT) e papel (requireRole).
 *  - API Composition: agrega dados de vários MS (logout, /clientes/:cpf R13,
 *    relatório R16, carteira do gerente R12, dashboard R15, extrato enriquecido R8).
 *  - SAGA Orquestrada: operações distribuídas são encaminhadas ao ms-saga
 *    (autocadastro R1, aprovar R10, alterar perfil R4, inserir/alterar/remover gerente R17/R20/R18).
 *
 * Mapa de requisitos → rota:
 *  R1  POST /clientes (saga)        R2  POST /login, /logout
 *  R4  PUT  /clientes/:cpf (saga)   R8  GET  /contas/:numero/extrato
 *  R3/R5/R6/R7 /contas/:numero/*    R9/R12/R14/R16 GET /clientes (por filtro)
 *  R10 POST /clientes/:cpf/aprovar  R11 POST /clientes/:cpf/rejeitar
 *  R13 GET  /clientes/:cpf          R15 GET /gerentes?filtro=dashboard
 *  R17 POST /gerentes (saga)        R18 DELETE /gerentes/:cpf (saga)
 *  R19 GET  /gerentes               R20 PUT /gerentes/:cpf (saga)
 *  Reboot: GET /reboot (fan-out resiliente para todos os MS).
 */
import { Router, Request, Response } from 'express';
import httpClient from '../lib/httpClient.js';
import services from '../config/services.js';
import verifyJWT from '../middleware/auth.js';
import requireRole from '../middleware/roleGuard.js';
import { revokeToken, unrevokeToken } from '../lib/revokedTokens.js';

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
  return { cpf: g.cpf, nome: g.nome, email: g.email, telefone: g.telefone, tipo: g.tipo ?? g.cargo };
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
  // Reativa o token caso seja idêntico a um revogado num logout anterior do mesmo usuário.
  if (data?.access_token) unrevokeToken(data.access_token);
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
  // O contrato do testador usa "ADMINISTRADOR"; o ms-auth devolve "ADMIN".
  if (data.tipo === 'ADMIN') data.tipo = 'ADMINISTRADOR';
  res.status(200).json(data);
});

// R2 — Logout (API Composition): JWT stateless. O gateway monta o LogoutResponse
// {cpf, nome, email, tipo} a partir do token (email/tipo) + busca por e-mail
// (cpf/nome) em ms-cliente ou ms-funcionario. Não chama o ms-auth (nada a fazer
// num JWT stateless) — evita o 415 e o descarte do token é responsabilidade do cliente.
router.post('/logout', verifyJWT, async (req, res) => {
  const email = req.user!.login;
  const tipo = req.user!.tipo;
  // Revoga o token (JWT stateless): chamadas seguintes com ele retornam 401.
  const parts = (req.headers.authorization ?? '').split(' ');
  if (parts.length === 2) revokeToken(parts[1]);
  let nome: string | null = null, cpf: string | null = null;
  try {
    if (tipo === 'CLIENTE') {
      const c = await get(`${services.cliente}/clientes/por-email/${email}`, H(req));
      if (c.status < 400) { nome = c.data.nome; cpf = c.data.cpf; }
    } else {
      const g = await get(`${services.funcionario}/funcionarios/por-email/${email}`, H(req));
      if (g.status < 400) { nome = g.data.nome; cpf = g.data.cpf; }
    }
  } catch { /* enriquecimento é best-effort */ }
  res.status(200).json({ cpf, nome, email, tipo });
});

// ════════════════════ REBOOT (fan-out) ════════════════════
const sleep = (ms: number) => new Promise((r) => setTimeout(r, ms));

/**
 * POST resiliente: tenta até `tentativas` vezes com `intervaloMs` entre elas.
 * Cobre a corrida de cold-start (o /reboot pode ser chamado logo após o
 * `docker compose up`, antes de um MS aceitar HTTP) — sem isso, o seed de um
 * serviço lento falharia silenciosamente e a base ficaria vazia.
 */
async function postComRetry(url: string, tentativas = 10, intervaloMs = 2000): Promise<boolean> {
  for (let i = 0; i < tentativas; i++) {
    try {
      const r = await httpClient.post(url);
      if (r.status < 400) return true;
    } catch { /* serviço ainda subindo — tenta de novo */ }
    if (i < tentativas - 1) await sleep(intervaloMs);
  }
  return false;
}

async function reboot(_req: Request, res: Response): Promise<void> {
  const alvos: Array<[string, string]> = [
    ['auth', services.auth + '/auth/reboot'],
    ['cliente', services.cliente + '/reboot'],
    ['conta', services.conta + '/reboot'],
    ['funcionario', services.funcionario + '/reboot'],
  ];
  const oks = await Promise.all(alvos.map(([, url]) => postComRetry(url)));
  const resumo = alvos.map(([nome], i) => ({ servico: nome, ok: oks[i] }));
  const todosOk = resumo.every((s) => s.ok);
  res.status(todosOk ? 200 : 503).json({ status: 'reboot', servicos: resumo });
}
router.get('/reboot', reboot);

// ════════════════════ CLIENTES ════════════════════
// R1 — Autocadastro (público) → SAGA bloqueante. A saga devolve SagaResultadoDTO
// (201/409); o contrato test_dac espera {cpf, email} no topo — remontamos a partir
// do corpo enviado, preservando o status.
router.post('/clientes', async (req, res) => {
  const r = await httpClient.post(`${services.saga}/saga/autocadastro`, req.body);
  if (r.status >= 400) { res.status(r.status).json(r.data); return; }
  res.status(r.status).json({ cpf: req.body?.cpf, email: req.body?.email });
});

// Listagens (R9/R12/R14/R16)
router.get('/clientes', verifyJWT, async (req, res) => {
  const filtro = req.query.filtro;
  if (filtro === 'adm_relatorio_clientes') {
    // R16 — somente ADMIN
    if (req.user!.tipo !== 'ADMIN') {
      res.status(403).json({ error: 'AuthorizationError', message: 'Acesso restrito ao perfil ADMIN.' });
      return;
    }
    return relatorioClientes(req, res);
  }
  // R9/R14 — filtros de gerente repassados ao ms-cliente
  if (filtro === 'para_aprovar' || filtro === 'melhores_clientes') {
    return forward(req, res, services.cliente, '/clientes');
  }
  // R12 — sem filtro (gerente): apenas a carteira do gerente logado, ordenada por nome
  return carteiraGerente(req, res);
});

// R12 — Lista os clientes (ativos) do gerente autenticado, ordenada por nome.
async function carteiraGerente(req: Request, res: Response): Promise<void> {
  const headers = H(req);
  const ger = await get(`${services.funcionario}/funcionarios/por-email/${req.user!.login}`, headers);
  if (ger.status >= 400) { res.json([]); return; }
  const contasRes = await get(`${services.conta}/contas/por-gerente/${ger.data.uuid ?? ger.data.id}`, headers);
  const contas = (contasRes.status < 400 ? contasRes.data : []).filter((c: any) => c.ativo);
  const result = await Promise.all((contas as any[]).map(async (c) => {
    const cli = await get(`${services.cliente}/clientes/por-cpf/${c.clienteCpf}`, headers);
    const d = cli.status < 400 ? cli.data : {};
    return {
      cpf: d.cpf ?? c.clienteCpf, nome: d.nome, email: d.email,
      conta: c.numero, saldo: c.saldo, limite: c.limite, criacao: c.dataCriacao,
    };
  }));
  result.sort((a, b) => (a.nome || '').localeCompare(b.nome || ''));
  res.json(result);
}

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
  // O contrato test_dac (R4) espera os dados alterados no corpo (cpf/nome/salario).
  res.status(200).json({
    cpf: req.params.cpf, nome: req.body?.nome, email: req.body?.email, salario: req.body?.salario,
  });
});

// R13 — Detalhe do cliente (API Composition) → DadosClienteResponse (plano)
router.get('/clientes/:cpf', verifyJWT, async (req, res) => {
  const cli = await get(`${services.cliente}/clientes/por-cpf/${req.params.cpf}`, H(req));
  if (cli.status >= 400) { res.status(cli.status).json(cli.data); return; }
  const c = cli.data;
  const conta = await get(`${services.conta}/contas/cliente/${c.uuid}`, H(req));
  const ct = conta.status < 400 ? conta.data : null;
  // R13 — só cliente aprovado (conta ativa) existe para consulta; rejeitado/pendente → 404.
  if (!ct || !ct.ativo) { res.status(404).json({ message: 'Cliente não encontrado' }); return; }
  let gCpf = null, gNome = null, gEmail = null;
  if (ct?.uuidGerente) {
    const g = await get(`${services.funcionario}/gerentes/${ct.uuidGerente}`, H(req));
    if (g.status < 400) { gCpf = g.data.cpf; gNome = g.data.nome; gEmail = g.data.email; }
  }
  res.json({
    cpf: c.cpf, nome: c.nome, telefone: c.telefone ?? null, email: c.email,
    endereco: c.endereco, cep: c.cep ?? null, cidade: c.cidade, estado: c.estado, salario: c.salario,
    conta: ct?.numero ?? null, saldo: ct?.saldo ?? null, limite: ct?.limite ?? null,
    criacao: ct?.dataCriacao ?? null, // "Conta desde" — data de abertura da conta
    gerente: gCpf, gerente_nome: gNome, gerente_email: gEmail,
  });
});

// R16 — Relatório de clientes (admin): cliente + conta + gerente
async function relatorioClientes(req: Request, res: Response): Promise<void> {
  const headers = H(req);
  const contasRes = await get(`${services.conta}/contas`, headers);
  // R16 — só clientes aprovados (conta ativa); pendentes/rejeitados ficam de fora.
  const contas = (contasRes.status < 400 ? contasRes.data : []).filter((c: any) => c.ativo);
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
      endereco: cli.endereco, cep: cli.cep ?? null, cidade: cli.cidade, estado: cli.estado, salario: cli.salario,
      conta: c.numero, saldo: c.saldo, limite: c.limite, criacao: c.dataCriacao,
      gerente: gCpf, gerente_nome: gNome, gerente_email: gEmail,
    };
  }));
  result.sort((a, b) => (a.nome || '').localeCompare(b.nome || ''));
  res.json(result);
}

// ════════════════════ CONTAS ════════════════════
// R8 — Extrato (API Composition): ms-conta entrega data(timestamp)/tipo/origem/destino/valor;
// o gateway enriquece cada movimentação com o NOME do titular das contas origem/destino
// (origemNome/destinoNome) para a UI exibir "Nome (número)". Rota específica ANTES do
// passthrough genérico de /contas (Express casa por ordem).
router.get('/contas/:numero/extrato', verifyJWT, async (req, res) => {
  const headers = H(req);
  const ext = await get(`${services.conta}/contas/${req.params.numero}/extrato`, headers);
  if (ext.status >= 400) { res.status(ext.status).json(ext.data); return; }
  const data = ext.data;

  // mapa número-da-conta → cpf (uma chamada) + resolução de nome por cpf (com cache)
  const contasRes = await get(`${services.conta}/contas`, headers);
  const numToCpf = new Map<string, string>();
  if (contasRes.status < 400) for (const c of contasRes.data as any[]) numToCpf.set(c.numero, c.clienteCpf);
  const nomeCache = new Map<string, string | null>();
  async function nomeDaConta(numero: string | null): Promise<string | null> {
    if (!numero) return null;
    const cpf = numToCpf.get(numero);
    if (!cpf) return null;
    if (!nomeCache.has(cpf)) {
      const cli = await get(`${services.cliente}/clientes/por-cpf/${cpf}`, headers);
      nomeCache.set(cpf, cli.status < 400 ? (cli.data?.nome ?? null) : null);
    }
    return nomeCache.get(cpf) ?? null;
  }

  data.movimentacoes = await Promise.all((data.movimentacoes as any[]).map(async (m) => ({
    ...m,
    origemNome: await nomeDaConta(m.origem),
    destinoNome: await nomeDaConta(m.destino),
  })));
  res.json(data);
});

// Demais operações de conta (saldo/depositar/sacar/transferir) — passthrough por número
router.use('/contas', verifyJWT, (req, res) => forward(req, res, services.conta, '/contas' + req.url));

// ════════════════════ GERENTES ════════════════════
// R17 — Inserir → SAGA; resposta = DadoGerente
router.post('/gerentes', verifyJWT, requireRole('ADMIN'), async (req, res) => {
  const r = await httpClient.post(`${services.saga}/saga/gerentes`, req.body);
  if (!r.data?.sucesso) {
    // CPF/e-mail já cadastrado → 409 (R17); demais falhas → 400.
    const erro = String(r.data?.erro ?? '');
    const status = /cadastrad|existe|conflito|duplicad/i.test(erro) ? 409 : 400;
    res.status(status).json({ error: 'insercao_falhou', message: erro });
    return;
  }
  res.status(201).json({ cpf: req.body.cpf, nome: req.body.nome, email: req.body.email, tipo: req.body.tipo ?? 'GERENTE' });
});

// R19 / R15 — Listar gerentes ou dashboard
router.get('/gerentes', verifyJWT, requireRole('ADMIN', 'GERENTE'), async (req, res) => {
  if (req.query.filtro === 'dashboard') return dashboard(req, res);
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
    // Apenas contas ativas: clientes pendentes/rejeitados não entram na carteira (R15/R18).
    const contas = (contasRes.status < 400 ? contasRes.data : []).filter((c: any) => c.ativo);
    let pos = 0, neg = 0;
    const clientes = (contas as any[]).map((c) => {
      const s = Number(c.saldo) || 0;
      if (s >= 0) pos += s; else neg += s;
      return { cliente: c.clienteCpf, numero: c.numero, saldo: c.saldo, limite: c.limite, gerente: g.cpf, criacao: c.dataCriacao };
    });
    return { gerente: dadoGerente(g), clientes, saldo_positivo: pos, saldo_negativo: neg };
  }));
  // Ordena pelo maior saldo positivo (R15).
  items.sort((a, b) => b.saldo_positivo - a.saldo_positivo);
  res.json(items);
}

export default router;
