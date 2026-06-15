/**
 * Especificação OpenAPI do API Gateway BANTADS.
 *
 * Definida como objeto EXPLÍCITO (não por varredura de JSDoc): a imagem de
 * produção contém apenas `dist/`, sem os arquivos-fonte `.ts`, então o
 * scanning de anotações ficaria vazio em runtime. Mantendo o spec inline ele
 * é compilado junto e sempre servido em `/api-docs`.
 *
 * Alinhado ao contrato oficial `test_dac 1.0.2`: schemas de resposta, respostas
 * de erro (ErroNaoLogado/ErroProibido), rotas de conta por número e descrições.
 */

const bearer = [{ bearerAuth: [] as string[] }];

const r = (description: string) => ({ description });
/** Resposta 200 com corpo `application/json` referenciando uma schema. */
const ok = (ref: string, description = 'OK') => ({
  description,
  content: { 'application/json': { schema: { $ref: `#/components/schemas/${ref}` } } },
});
const naoLogado = { $ref: '#/components/responses/ErroNaoLogado' };
const proibido = { $ref: '#/components/responses/ErroProibido' };

const dbl = { type: 'number', format: 'double' } as const;

export const swaggerSpec = {
  openapi: '3.0.3',
  info: {
    title: 'BANTADS API Gateway',
    version: '1.0.2',
    description:
      'Gateway de entrada do sistema bancário BANTADS (porta 3000). Centraliza ' +
      'autenticação (JWT), roteamento para os microsserviços, encaminhamento de ' +
      'SAGAs (ms-saga), API Composition e o fan-out de /reboot.',
  },
  servers: [
    { url: 'http://localhost:8000', description: 'Local (porta exposta — testador)' },
    { url: 'http://localhost:3000', description: 'Local (porta interna)' },
  ],
  tags: [
    { name: 'Inicialização', description: 'Inicialização da base de dados (reboot — fan-out a todos os MS)' },
    { name: 'Login', description: 'Autenticação e sessão (R2)' },
    { name: 'Clientes', description: 'Clientes, aprovação/rejeição e perfil (R1/R4/R9-R14/R16)' },
    { name: 'Contas', description: 'Contas, saldo, extrato e operações (R3/R5-R8)' },
    { name: 'Gerentes', description: 'Gerentes e dashboard (R15/R17-R20)' },
  ],
  components: {
    securitySchemes: {
      bearerAuth: { type: 'http', scheme: 'bearer', bearerFormat: 'JWT', description: 'Token obtido em POST /login' },
    },
    responses: {
      ErroNaoLogado: { description: 'O usuário não está logado' },
      ErroProibido: { description: 'O usuário não tem permissão para efetuar esta operação' },
    },
    schemas: {
      // ── Requisições ──
      LoginInfo: {
        type: 'object',
        properties: { login: { type: 'string', example: 'cli1@bantads.com.br' }, senha: { type: 'string', example: 'tads' } },
      },
      AutocadastroInfo: {
        type: 'object',
        properties: {
          cpf: { type: 'string' },
          email: { type: 'string' },
          nome: { type: 'string' },
          telefone: { type: 'string' },
          salario: dbl,
          endereco: { type: 'string' },
          CEP: { type: 'string' },
          cidade: { type: 'string' },
          estado: { type: 'string' },
        },
      },
      PerfilInfo: {
        type: 'object',
        properties: {
          nome: { type: 'string', example: 'Crysthôncio' },
          email: { type: 'string', example: 'crys@bantads.com.br' },
          salario: { ...dbl, example: 1506.88 },
          endereco: { type: 'string', example: 'Rua das Palmeiras, 500' },
          CEP: { type: 'string', example: '80000000' },
          cidade: { type: 'string', example: 'Curitiba' },
          estado: { type: 'string', example: 'PR' },
        },
      },
      DadoGerenteInsercao: {
        type: 'object',
        properties: {
          cpf: { type: 'string', description: 'CPF do gerente', example: '40501740066' },
          nome: { type: 'string', description: 'Nome do gerente', example: 'Geniéve' },
          email: { type: 'string', description: 'Email do gerente', example: 'ger1@bantads.com.br' },
          tipo: { type: 'string', description: 'Tipo do gerente', example: 'ADMINISTRADOR' },
          senha: { type: 'string', description: 'Senha do novo gerente', example: 'tads' },
        },
      },
      DadoGerenteAtualizacao: {
        type: 'object',
        properties: {
          nome: { type: 'string', description: 'Nome do gerente', example: 'Geniéve' },
          email: { type: 'string', description: 'Email do gerente', example: 'ger1@bantads.com.br' },
          senha: { type: 'string', description: 'Senha do novo gerente', example: 'tads' },
        },
      },

      // ── Login / Logout ──
      LoginResponse: {
        type: 'object',
        properties: {
          access_token: { type: 'string' },
          token_type: { type: 'string', example: 'bearer' },
          tipo: { type: 'string', enum: ['CLIENTE', 'GERENTE', 'ADMIN'] },
          usuario: {
            type: 'object',
            properties: {
              nome: { type: 'string', example: 'Catharyna' },
              cpf: { type: 'string', example: '12912861012' },
              email: { type: 'string', example: 'cli1@bantads.com.br' },
            },
          },
        },
      },
      LogoutResponse: {
        type: 'object',
        properties: {
          cpf: { type: 'string' },
          nome: { type: 'string' },
          email: { type: 'string' },
          tipo: { type: 'string' },
        },
      },

      // ── Clientes ──
      TodosClientesResponse: { type: 'array', items: { $ref: '#/components/schemas/ClienteResponse' } },
      ParaAprovarResponse: { type: 'array', items: { $ref: '#/components/schemas/ClienteParaAprovarResponse' } },
      RelatorioClientesResponse: { type: 'array', items: { $ref: '#/components/schemas/DadosClienteResponse' } },
      ClienteResponse: {
        type: 'object',
        properties: {
          cpf: { type: 'string', example: '12912861012' },
          nome: { type: 'string', example: 'Catharyna' },
          email: { type: 'string', example: 'cli1@bantads.com.br' },
          telefone: { type: 'string', example: '(41) 9 9999-8989' },
          endereco: { type: 'string', example: 'Rua X, nr 10' },
          cidade: { type: 'string', example: 'Curitiba' },
          estado: { type: 'string', example: 'PR' },
          conta: { type: 'string', example: '1291' },
          saldo: { ...dbl, example: 800.0 },
          limite: { ...dbl, example: 5000.0 },
        },
      },
      ClienteParaAprovarResponse: {
        type: 'object',
        properties: {
          cpf: { type: 'string', example: '12912861012' },
          nome: { type: 'string', example: 'Catharyna' },
          email: { type: 'string', example: 'cli1@bantads.com.br' },
          salario: { ...dbl, example: 5000.0 },
          endereco: { type: 'string', example: 'Rua X, nr 10' },
          cidade: { type: 'string', example: 'Curitiba' },
          estado: { type: 'string', example: 'PR' },
        },
      },
      DadosClienteResponse: {
        type: 'object',
        properties: {
          cpf: { type: 'string', example: '12912861012' },
          nome: { type: 'string', example: 'Catharyna' },
          telefone: { type: 'string', example: '(41) 9 9999-8989' },
          email: { type: 'string', example: 'cli1@bantads.com.br' },
          endereco: { type: 'string', example: 'Rua X, nr 10' },
          cidade: { type: 'string', example: 'Curitiba' },
          estado: { type: 'string', example: 'PR' },
          salario: { ...dbl, example: 800.0 },
          conta: { type: 'string', example: '1291' },
          saldo: { ...dbl, example: 800.0 },
          limite: { ...dbl, example: 5000.0 },
          gerente: { type: 'string', example: '98574307084' },
          gerente_nome: { type: 'string', example: 'Geniéve' },
          gerente_email: { type: 'string', example: 'ger1@bantads.com.br' },
        },
      },

      // ── Contas ──
      SaldoResponse: {
        type: 'object',
        properties: {
          cliente: { type: 'string', example: '76179646090' },
          conta: { type: 'string', example: '0556' },
          saldo: { ...dbl, example: 1506.88 },
        },
      },
      ContaResponse: {
        type: 'object',
        properties: {
          cliente: { type: 'string', example: '76179646090' },
          numero: { type: 'string', example: '0114' },
          saldo: { ...dbl, example: 2000.0 },
          limite: { ...dbl, example: 1000.0 },
          gerente: { type: 'string', example: '98574307084' },
          criacao: { type: 'string', format: 'date', description: 'Data de abertura da conta (apenas data)', example: '2000-01-01' },
        },
      },
      OperacaoResponse: {
        type: 'object',
        properties: {
          conta: { type: 'string', example: '1291' },
          data: { type: 'string', example: '2025-08-01T10:22:45-03:00' },
          saldo: { ...dbl, example: 1044.23 },
        },
      },
      TransferenciaResponse: {
        type: 'object',
        properties: {
          conta: { type: 'string', example: '1291' },
          data: { type: 'string', example: '2025-08-01T10:22:45-03:00' },
          destino: { type: 'string', example: '4321' },
          saldo: { ...dbl, example: 1044.23 },
          valor: { ...dbl, example: 87.12 },
        },
      },
      ExtratoResponse: {
        type: 'object',
        properties: {
          conta: { type: 'string', description: 'Número da conta', example: '8722' },
          saldo: { ...dbl, description: 'Saldo final da conta', example: 1933.32 },
          movimentacoes: { type: 'array', items: { $ref: '#/components/schemas/ItemExtratoResponse' } },
        },
      },
      ItemExtratoResponse: {
        type: 'object',
        properties: {
          data: { type: 'string', description: 'Data da movimentação', example: '2025-08-01T10:22:45-03:00' },
          tipo: { type: 'string', description: 'Tipo da movimentação', enum: ['saque', 'depósito', 'transferência'] },
          origem: { type: 'string', description: 'Conta origem dos valores', example: '8872' },
          origemNome: { type: 'string', description: 'Nome do titular da conta origem (composição)', example: 'Catharyna' },
          destino: { type: 'string', description: 'Conta destino dos valores', example: '9961' },
          destinoNome: { type: 'string', description: 'Nome do titular da conta destino (composição)', example: 'Cleuddônio' },
          valor: { ...dbl, example: 972.22 },
        },
      },

      // ── Gerentes / Dashboard ──
      DashboardResponse: { type: 'array', items: { $ref: '#/components/schemas/ItemDashboardResponse' } },
      GerentesResponse: { type: 'array', items: { $ref: '#/components/schemas/DadoGerente' } },
      ItemDashboardResponse: {
        type: 'object',
        properties: {
          gerente: { $ref: '#/components/schemas/DadoGerente' },
          clientes: { type: 'array', items: { $ref: '#/components/schemas/DadoConta' } },
          saldo_positivo: { ...dbl, description: 'Saldo positivo das suas contas', example: 89123.22 },
          saldo_negativo: { ...dbl, description: 'Saldo negativo das suas contas', example: -982.35 },
        },
      },
      DadoConta: {
        type: 'object',
        properties: {
          cliente: { type: 'string', description: 'CPF do cliente', example: '40501740066' },
          numero: { type: 'string', description: 'Número da conta', example: '7811' },
          saldo: { ...dbl, description: 'Saldo da conta', example: 1099.21 },
          limite: { ...dbl, description: 'Limite da conta', example: 5000.0 },
          gerente: { type: 'string', description: 'CPF do gerente', example: '23862179060' },
          criacao: { type: 'string', format: 'date', description: 'Data de abertura da conta (apenas data)', example: '2000-01-01' },
        },
      },
      DadoGerente: {
        type: 'object',
        properties: {
          cpf: { type: 'string', description: 'CPF do gerente', example: '40501740066' },
          nome: { type: 'string', description: 'Nome do gerente', example: 'Geniéve' },
          email: { type: 'string', description: 'Email do gerente', example: 'ger1@bantads.com.br' },
          tipo: { type: 'string', description: 'Tipo do gerente', example: 'ADMINISTRADOR' },
        },
      },
    },
  },
  paths: {
    // ── Inicialização ──
    '/reboot': {
      get: {
        tags: ['Inicialização'], summary: 'Inicia o banco de dados', description: 'inicia o banco de dados (fan-out a todos os MS)',
        security: [], responses: { 200: r('Banco de dados criado conforme especificação') },
      },
    },

    // ── Login ──
    '/login': {
      post: {
        tags: ['Login'], summary: 'Login (R2)', description: 'efetua o login do usuário (API Composition: ms-auth + ms-cliente/ms-funcionario)',
        security: [],
        requestBody: { content: { 'application/json': { schema: { $ref: '#/components/schemas/LoginInfo' } } } },
        responses: { 200: ok('LoginResponse', 'Login efetuado com sucesso'), 401: r('Usuário/Senha incorretos') },
      },
    },
    '/logout': {
      post: {
        tags: ['Login'], summary: 'Logout (R2)', description: 'efetua o logout do usuário (API Composition)',
        security: bearer,
        responses: { 200: ok('LogoutResponse', 'Logout efetuado com sucesso'), 401: naoLogado },
      },
    },

    // ── Clientes ──
    '/clientes': {
      get: {
        tags: ['Clientes'], summary: 'Consulta clientes (R9/R12/R14/R16)', description: 'consulta todos os clientes',
        security: bearer,
        parameters: [{
          in: 'query', name: 'filtro', required: false,
          description:
            'Define qual tipo de busca será feita.\n' +
            '   - _para_aprovar_: (GERENTE) busca os clientes que aguardam aprovação\n' +
            '   - _adm_relatorio_clientes_: (ADMINISTRADOR) busca todos os clientes para tirar o relatório, com dados de cliente, conta e gerente\n' +
            '   - _melhores_clientes_: (GERENTE) busca os 3 melhores clientes\n' +
            '   - (sem filtro): (GERENTE) busca todos os clientes com conta',
          schema: { type: 'string', enum: ['para_aprovar', 'adm_relatorio_clientes', 'melhores_clientes'] },
        }],
        responses: {
          200: {
            description: 'Clientes retornados com sucesso',
            content: {
              'application/json': {
                schema: {
                  oneOf: [
                    { $ref: '#/components/schemas/ParaAprovarResponse' },
                    { $ref: '#/components/schemas/RelatorioClientesResponse' },
                    { $ref: '#/components/schemas/TodosClientesResponse' },
                  ],
                },
              },
            },
          },
          401: naoLogado,
          403: proibido,
        },
      },
      post: {
        tags: ['Clientes'], summary: 'Autocadastro (R1)', description: 'autocadastro de cliente',
        security: [],
        requestBody: { content: { 'application/json': { schema: { $ref: '#/components/schemas/AutocadastroInfo' } } } },
        responses: {
          201: r('Cliente autocadastrado'),
          400: r('Erro inserindo cliente'),
          409: r('Cliente já cadastrado ou aguardando aprovação, CPF duplicado'),
        },
      },
    },
    '/clientes/{cpf}': {
      get: {
        tags: ['Clientes'], summary: 'Consulta um cliente (R13)', description: 'Consulta um cliente (API Composition: cliente+conta+gerente)',
        security: bearer,
        parameters: [{ in: 'path', name: 'cpf', required: true, schema: { type: 'string' } }],
        responses: {
          200: ok('DadosClienteResponse', 'Usuário consultado com sucesso'),
          401: naoLogado, 403: proibido, 404: r('Usuário não encontrado'),
        },
      },
      put: {
        tags: ['Clientes'], summary: 'Altera perfil (R4)', description: 'altera os dados de perfil de cliente (SAGA)',
        security: bearer,
        parameters: [{ in: 'path', name: 'cpf', required: true, schema: { type: 'string' } }],
        requestBody: { content: { 'application/json': { schema: { $ref: '#/components/schemas/PerfilInfo' } } } },
        responses: { 200: r('Perfil do cliente alterado com sucesso'), 401: naoLogado, 403: proibido },
      },
    },
    '/clientes/{cpf}/aprovar': {
      post: {
        tags: ['Clientes'], summary: 'Aprovar cliente (R10)', description: 'aprova o cliente com o cpf passado (SAGA)',
        security: bearer,
        parameters: [{ in: 'path', name: 'cpf', required: true, description: 'CPF do cliente a ser aprovado', schema: { type: 'string' } }],
        responses: { 200: ok('ContaResponse', 'Cliente aprovado com sucesso'), 401: naoLogado, 403: proibido },
      },
    },
    '/clientes/{cpf}/rejeitar': {
      post: {
        tags: ['Clientes'], summary: 'Rejeitar cliente (R11)', description: 'rejeita o cliente com o cpf passado',
        security: bearer,
        parameters: [{ in: 'path', name: 'cpf', required: true, description: 'CPF do cliente a ser rejeitado', schema: { type: 'string' } }],
        requestBody: {
          content: {
            'application/json': {
              schema: { type: 'object', properties: { motivo: { type: 'string', example: 'Usuário não é interessante para o banco' } } },
            },
          },
        },
        responses: { 200: r('Cliente rejeitado com sucesso'), 401: naoLogado, 403: proibido },
      },
    },

    // ── Contas (por número — passthrough ao ms-conta) ──
    '/contas/{numero}/saldo': {
      get: {
        tags: ['Contas'], summary: 'Saldo da conta (R3)', description: 'retorna o saldo da conta',
        security: bearer,
        parameters: [{ in: 'path', name: 'numero', required: true, description: 'Número da conta', schema: { type: 'string' } }],
        responses: {
          200: ok('SaldoResponse', 'Saldo retornado com sucesso'),
          400: r('Filtro Não Existe'), 401: naoLogado, 403: proibido, 404: r('Clientes não encontradas'),
        },
      },
    },
    '/contas/{numero}/depositar': {
      post: {
        tags: ['Contas'], summary: 'Depositar (R5)', description: 'deposita um valor na conta do cliente',
        security: bearer,
        parameters: [{ in: 'path', name: 'numero', required: true, description: 'Número da conta', schema: { type: 'string' } }],
        requestBody: {
          content: { 'application/json': { schema: { type: 'object', properties: { valor: { ...dbl, description: 'Valor a ser depositado', example: 51.44 } } } } },
        },
        responses: { 200: ok('OperacaoResponse', 'Depósito efetuado com sucesso'), 401: naoLogado, 403: proibido },
      },
    },
    '/contas/{numero}/sacar': {
      post: {
        tags: ['Contas'], summary: 'Sacar (R6)', description: 'Saca um valor da conta do cliente',
        security: bearer,
        parameters: [{ in: 'path', name: 'numero', required: true, description: 'Número da conta', schema: { type: 'string' } }],
        requestBody: {
          content: { 'application/json': { schema: { type: 'object', properties: { valor: { ...dbl, description: 'Valor a ser sacado', example: 51.44 } } } } },
        },
        responses: { 200: ok('OperacaoResponse', 'Saque efetuado com sucesso'), 401: naoLogado, 403: proibido },
      },
    },
    '/contas/{numero}/transferir': {
      post: {
        tags: ['Contas'], summary: 'Transferir (R7)', description: 'Transfere um valor da conta do cliente',
        security: bearer,
        parameters: [{ in: 'path', name: 'numero', required: true, description: 'Número da conta', schema: { type: 'string' } }],
        requestBody: {
          content: {
            'application/json': {
              schema: {
                type: 'object',
                properties: {
                  destino: { type: 'string', example: '0123', description: 'Conta destino da transferência' },
                  valor: { ...dbl, description: 'Valor a ser transferido', example: 51.44 },
                },
              },
            },
          },
        },
        responses: { 200: ok('TransferenciaResponse', 'Transferência efetuada com sucesso'), 401: naoLogado, 403: proibido },
      },
    },
    '/contas/{numero}/extrato': {
      get: {
        tags: ['Contas'], summary: 'Extrato (R8)', description: 'Retorna o extrato da conta',
        security: bearer,
        parameters: [{ in: 'path', name: 'numero', required: true, description: 'Número da conta', schema: { type: 'string' } }],
        responses: { 200: ok('ExtratoResponse', 'Extrato retornado com sucesso'), 401: naoLogado, 403: proibido },
      },
    },

    // ── Gerentes ──
    '/gerentes': {
      get: {
        tags: ['Gerentes'], summary: 'Lista gerentes (R19) / dashboard (R15)', description: 'Busca todos os gerentes cadastrados',
        security: bearer,
        parameters: [{
          in: 'query', name: 'numero', required: false,
          description: 'Se é para retornar todos os gerentes ou se a consulta é para dashboard\n- *dashboard*: retorna os dados para montar o dashboard do gerente\n- sem filtro: retorna todos os dados de gerentes',
          schema: { type: 'string', enum: ['dashboard'] },
        }],
        responses: {
          200: {
            description: 'Retorna a lista de gerentes',
            content: {
              'application/json': {
                schema: { oneOf: [{ $ref: '#/components/schemas/DashboardResponse' }, { $ref: '#/components/schemas/GerentesResponse' }] },
              },
            },
          },
          401: naoLogado, 403: proibido,
        },
      },
      post: {
        tags: ['Gerentes'], summary: 'Inserir gerente (R17)', description: 'Inserção de Gerentes (ADMINISTRADOR) — SAGA',
        security: bearer,
        requestBody: { content: { 'application/json': { schema: { $ref: '#/components/schemas/DadoGerenteInsercao' } } } },
        responses: { 200: ok('DadoGerente', 'Gerente inserido com sucesso'), 401: naoLogado, 403: proibido },
      },
    },
    '/gerentes/{cpf}': {
      get: {
        tags: ['Gerentes'], summary: 'Consulta gerente', description: 'Consulta um gerente (ADMINISTRADOR)',
        security: bearer,
        parameters: [{ in: 'path', name: 'cpf', required: true, description: 'CPF do gerente a ser consultado', schema: { type: 'string' } }],
        responses: { 200: ok('DadoGerente', 'Gerente encontrado e dados retornados com sucesso'), 401: naoLogado, 403: proibido, 404: r('Gerente não encontrado') },
      },
      put: {
        tags: ['Gerentes'], summary: 'Alterar gerente (R20)', description: 'Atualiza os dados de um gerente (ADMINISTRADOR) — SAGA',
        security: bearer,
        parameters: [{ in: 'path', name: 'cpf', required: true, description: 'CPF do gerente a ser atualizado', schema: { type: 'string' } }],
        requestBody: { content: { 'application/json': { schema: { $ref: '#/components/schemas/DadoGerenteAtualizacao' } } } },
        responses: { 200: ok('DadoGerente', 'Gerente encontrado e alterado com sucesso'), 401: naoLogado, 403: proibido, 404: r('Gerente não encontrado') },
      },
      delete: {
        tags: ['Gerentes'], summary: 'Remover gerente (R18)', description: 'Remove um gerente (ADMINISTRADOR) — SAGA',
        security: bearer,
        parameters: [{ in: 'path', name: 'cpf', required: true, description: 'CPF do gerente a ser removido', schema: { type: 'string' } }],
        responses: { 200: ok('DadoGerente', 'Gerente encontrado e removido com sucesso'), 401: naoLogado, 403: proibido, 404: r('Gerente não encontrado') },
      },
    },
  },
} as const;
