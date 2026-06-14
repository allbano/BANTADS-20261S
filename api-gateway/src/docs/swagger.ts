/**
 * Especificação OpenAPI do API Gateway BANTADS.
 *
 * Definida como objeto EXPLÍCITO (não por varredura de JSDoc): a imagem de
 * produção contém apenas `dist/`, sem os arquivos-fonte `.ts`, então o
 * scanning de anotações ficaria vazio em runtime. Mantendo o spec inline ele
 * é compilado junto e sempre servido em `/api-docs`.
 */

const bearer = [{ bearerAuth: [] as string[] }];

const r = (description: string) => ({ description });

export const swaggerSpec = {
  openapi: '3.0.3',
  info: {
    title: 'BANTADS API Gateway',
    version: '1.0.0',
    description:
      'Gateway de entrada do sistema bancário BANTADS (porta 3000). Centraliza ' +
      'autenticação (JWT), roteamento para os microsserviços, encaminhamento de ' +
      'SAGAs (ms-saga), API Composition e o fan-out de /reboot.',
  },
  servers: [{ url: 'http://localhost:3000', description: 'Local' }],
  tags: [
    { name: 'Inicialização', description: 'Inicialização da base de dados (reboot — fan-out a todos os MS)' },
    { name: 'Login', description: 'Autenticação e sessão (R2)' },
    { name: 'Clientes', description: 'Clientes, aprovação/rejeição e perfil (R1/R4/R9-R14)' },
    { name: 'Contas', description: 'Contas, saldo, extrato e operações (R3/R5-R8)' },
    { name: 'Gerentes', description: 'Gerentes e dashboard (R15/R17-R20)' },
  ],
  components: {
    securitySchemes: {
      bearerAuth: { type: 'http', scheme: 'bearer', bearerFormat: 'JWT', description: 'Token obtido em POST /login' },
    },
    schemas: {
      LoginRequest: {
        type: 'object', required: ['login', 'senha'],
        properties: { login: { type: 'string', example: 'adm1@bantads.com.br' }, senha: { type: 'string', example: 'tads' } },
      },
      LoginResponse: {
        type: 'object',
        properties: {
          access_token: { type: 'string' },
          token_type: { type: 'string', example: 'bearer' },
          tipo: { type: 'string', enum: ['CLIENTE', 'GERENTE', 'ADMIN'] },
          usuario: { type: 'object', properties: { email: { type: 'string' } } },
        },
      },
      Autocadastro: {
        type: 'object', required: ['nome', 'email', 'cpf'],
        properties: {
          nome: { type: 'string' }, email: { type: 'string' }, senha: { type: 'string' },
          cpf: { type: 'string' }, telefone: { type: 'string' }, salario: { type: 'number' },
          endereco: { type: 'string' }, cep: { type: 'string' }, cidade: { type: 'string' }, estado: { type: 'string' },
        },
      },
      AlterarPerfil: {
        type: 'object',
        properties: { nome: { type: 'string' }, email: { type: 'string' }, telefone: { type: 'string' }, salario: { type: 'number' }, endereco: { type: 'string' }, cep: { type: 'string' }, cidade: { type: 'string' }, estado: { type: 'string' } },
      },
      Rejeicao: { type: 'object', properties: { motivo: { type: 'string', example: 'Renda insuficiente' } } },
      GerenteRequest: {
        type: 'object', required: ['cpf', 'nome', 'email', 'senha'],
        properties: { cpf: { type: 'string' }, nome: { type: 'string' }, email: { type: 'string' }, telefone: { type: 'string' }, senha: { type: 'string' } },
      },
      AlterarGerente: { type: 'object', properties: { nome: { type: 'string' }, email: { type: 'string' }, telefone: { type: 'string' }, senha: { type: 'string' } } },
      Movimentacao: {
        type: 'object', required: ['tipo', 'valor'],
        properties: { tipo: { type: 'string', enum: ['DEPOSITO', 'SAQUE', 'TRANSFERENCIA'] }, valor: { type: 'number' }, uuidContaDestino: { type: 'string' } },
      },
      SagaIniciada: { type: 'object', properties: { sagaId: { type: 'string' }, status: { type: 'string', example: 'INICIADA' } } },
    },
  },
  paths: {
    // ── Inicialização ──
    '/reboot': {
      get: { tags: ['Inicialização'], summary: 'Inicia o banco de dados de todos os MS (fan-out)', security: [], responses: { 200: r('Banco de dados reinicializado em todos os microsserviços') } },
    },
    // ── Login ──
    '/login': {
      post: {
        tags: ['Login'], summary: 'Login (R2) — retorna JWT', security: [],
        requestBody: { required: true, content: { 'application/json': { schema: { $ref: '#/components/schemas/LoginRequest' } } } },
        responses: { 200: { description: 'OK', content: { 'application/json': { schema: { $ref: '#/components/schemas/LoginResponse' } } } }, 401: r('Credenciais inválidas') },
      },
    },
    '/logout': {
      post: { tags: ['Login'], summary: 'Logout (R2)', security: [], responses: { 200: r('OK') } },
    },
    // ── Clientes ──
    '/clientes': {
      get: {
        tags: ['Clientes'], summary: 'Lista clientes (R9/R12/R14/R16; aceita ?filtro=)', security: bearer,
        parameters: [{ in: 'query', name: 'filtro', schema: { type: 'string', enum: ['para_aprovar', 'melhores_clientes', 'adm_relatorio_clientes'] } }],
        responses: { 200: r('Lista de clientes'), 401: r('Sem token') },
      },
      post: {
        tags: ['Clientes'], summary: 'Autocadastro (R1) — inicia SAGA', security: [],
        requestBody: { required: true, content: { 'application/json': { schema: { $ref: '#/components/schemas/Autocadastro' } } } },
        responses: { 202: { description: 'SAGA iniciada', content: { 'application/json': { schema: { $ref: '#/components/schemas/SagaIniciada' } } } } },
      },
    },
    '/clientes/{cpf}': {
      get: {
        tags: ['Clientes'], summary: 'Detalhe do cliente (R13) — API Composition (cliente+conta+gerente)', security: bearer,
        parameters: [{ in: 'path', name: 'cpf', required: true, schema: { type: 'string' } }],
        responses: { 200: r('Cliente com conta e gerente'), 404: r('Não encontrado') },
      },
      put: {
        tags: ['Clientes'], summary: 'Alteração de perfil (R4) — inicia SAGA', security: bearer,
        parameters: [{ in: 'path', name: 'cpf', required: true, schema: { type: 'string' } }],
        requestBody: { required: true, content: { 'application/json': { schema: { $ref: '#/components/schemas/AlterarPerfil' } } } },
        responses: { 202: { description: 'SAGA iniciada', content: { 'application/json': { schema: { $ref: '#/components/schemas/SagaIniciada' } } } } },
      },
    },
    '/clientes/{cpf}/aprovar': {
      post: {
        tags: ['Clientes'], summary: 'Aprovar cliente (R10) — SAGA (GERENTE/ADMIN)', security: bearer,
        parameters: [{ in: 'path', name: 'cpf', required: true, schema: { type: 'string' } }],
        responses: { 202: r('SAGA iniciada'), 403: r('Sem permissão') },
      },
    },
    '/clientes/{cpf}/rejeitar': {
      post: {
        tags: ['Clientes'], summary: 'Rejeitar cliente (R11) — direto ms-cliente (GERENTE/ADMIN)', security: bearer,
        parameters: [{ in: 'path', name: 'cpf', required: true, schema: { type: 'string' } }],
        requestBody: { content: { 'application/json': { schema: { $ref: '#/components/schemas/Rejeicao' } } } },
        responses: { 200: r('Rejeitado'), 404: r('Não encontrado'), 403: r('Sem permissão') },
      },
    },
    // ── Contas ──
    '/contas/cliente/{uuidCliente}': {
      get: {
        tags: ['Contas'], summary: 'Conta do cliente (saldo/limite) — R3', security: bearer,
        parameters: [{ in: 'path', name: 'uuidCliente', required: true, schema: { type: 'string', format: 'uuid' } }],
        responses: { 200: r('Conta'), 404: r('Não encontrada') },
      },
    },
    '/contas/{uuidConta}/movimentacoes': {
      post: {
        tags: ['Contas'], summary: 'Depósito/Saque/Transferência (R5/R6/R7)', security: bearer,
        parameters: [{ in: 'path', name: 'uuidConta', required: true, schema: { type: 'string', format: 'uuid' } }],
        requestBody: { required: true, content: { 'application/json': { schema: { $ref: '#/components/schemas/Movimentacao' } } } },
        responses: { 200: r('Movimentação registrada'), 400: r('Saldo/limite insuficiente') },
      },
      get: {
        tags: ['Contas'], summary: 'Lista movimentações da conta', security: bearer,
        parameters: [{ in: 'path', name: 'uuidConta', required: true, schema: { type: 'string', format: 'uuid' } }],
        responses: { 200: r('Movimentações') },
      },
    },
    '/contas/{uuidConta}/extrato': {
      get: {
        tags: ['Contas'], summary: 'Extrato por período (R8)', security: bearer,
        parameters: [
          { in: 'path', name: 'uuidConta', required: true, schema: { type: 'string', format: 'uuid' } },
          { in: 'query', name: 'dataInicio', schema: { type: 'string', format: 'date' } },
          { in: 'query', name: 'dataFim', schema: { type: 'string', format: 'date' } },
        ],
        responses: { 200: r('Extrato') },
      },
    },
    // ── Gerentes ──
    '/gerentes': {
      get: { tags: ['Gerentes'], summary: 'Lista gerentes (R19) / dashboard (R15) — ADMIN/GERENTE', security: bearer, responses: { 200: r('Lista'), 403: r('Sem permissão') } },
      post: {
        tags: ['Gerentes'], summary: 'Inserir gerente (R17) — SAGA (ADMIN)', security: bearer,
        requestBody: { required: true, content: { 'application/json': { schema: { $ref: '#/components/schemas/GerenteRequest' } } } },
        responses: { 202: r('SAGA iniciada'), 403: r('Sem permissão') },
      },
    },
    '/gerentes/{cpf}': {
      get: {
        tags: ['Gerentes'], summary: 'Detalhe do gerente — ADMIN/GERENTE', security: bearer,
        parameters: [{ in: 'path', name: 'cpf', required: true, schema: { type: 'string' } }],
        responses: { 200: r('Gerente'), 404: r('Não encontrado') },
      },
      put: {
        tags: ['Gerentes'], summary: 'Alterar gerente (R20) — SAGA (ADMIN)', security: bearer,
        parameters: [{ in: 'path', name: 'cpf', required: true, schema: { type: 'string' } }],
        requestBody: { required: true, content: { 'application/json': { schema: { $ref: '#/components/schemas/AlterarGerente' } } } },
        responses: { 202: r('SAGA iniciada'), 403: r('Sem permissão') },
      },
      delete: {
        tags: ['Gerentes'], summary: 'Remover gerente (R18) — SAGA (ADMIN)', security: bearer,
        parameters: [{ in: 'path', name: 'cpf', required: true, schema: { type: 'string' } }],
        responses: { 202: r('SAGA iniciada'), 403: r('Sem permissão') },
      },
    },
  },
} as const;
