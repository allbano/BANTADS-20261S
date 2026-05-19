import cors from 'cors';

/**
 * Configuração CORS do API Gateway.
 * Define quais origens podem acessar a API e quais métodos/headers são permitidos.
 */
const corsOptions: cors.CorsOptions = {
  origin: [
    'http://localhost:4200', // Angular frontend (desenvolvimento)
    'http://localhost:3000', // Gateway (desenvolvimento)
  ],
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'PATCH'],
  allowedHeaders: ['Content-Type', 'Authorization'],
  credentials: true,
};

export default cors(corsOptions);
