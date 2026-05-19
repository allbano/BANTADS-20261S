import swaggerJsdoc from 'swagger-jsdoc';

/**
 * Configuração central do Swagger/OpenAPI.
 * Escaneia as anotações JSDoc dos arquivos de rota para gerar a especificação.
 */
const options: swaggerJsdoc.Options = {
  definition: {
    openapi: '3.0.3',
    info: {
      title: 'BANTADS API Gateway',
      version: '1.0.0',
      description:
        'Gateway de entrada do sistema bancário BANTADS. ' +
        'Centraliza autenticação, roteamento e composição de dados dos microsserviços.',
    },
    servers: [
      {
        url: 'http://localhost:3000',
        description: 'Desenvolvimento',
      },
    ],
    components: {
      securitySchemes: {
        bearerAuth: {
          type: 'http',
          scheme: 'bearer',
          bearerFormat: 'JWT',
          description: 'Token JWT obtido via POST /auth/login',
        },
      },
    },
    security: [{ bearerAuth: [] }],
    tags: [
      { name: 'Auth', description: 'Autenticação e gerenciamento de sessão' },
      { name: 'Clientes', description: 'Operações sobre clientes (proxy → ms-cliente)' },
      { name: 'Contas', description: 'Operações sobre contas bancárias (proxy → ms-conta)' },
      { name: 'Gerentes', description: 'Dados compostos de gerentes (API Composition → ms-funcionario + ms-conta)' },
    ],
  },
  apis: ['./src/routes/**/*.ts'],
};

export const swaggerSpec = swaggerJsdoc(options);
