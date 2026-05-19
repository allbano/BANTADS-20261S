import express from 'express';
import morgan from 'morgan';
import swaggerUi from 'swagger-ui-express';

import { PORT } from './config/env.js';
import corsMiddleware from './middleware/cors.js';
import errorHandler from './middleware/errorHandler.js';
import routes from './routes/index.js';
import { swaggerSpec } from './docs/swagger.js';

const app = express();

// ─── Middlewares globais ────────────────────────────────────────────
app.use(corsMiddleware);
app.use(morgan('combined'));
app.use(express.json());

// ─── Documentação Swagger ───────────────────────────────────────────
app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(swaggerSpec));

// ─── Rotas ──────────────────────────────────────────────────────────
app.use(routes);

// ─── Error handler global (deve ser o ÚLTIMO middleware) ────────────
app.use(errorHandler);

// ─── Start ──────────────────────────────────────────────────────────
app.listen(PORT, () => {
  console.log(`🚀 Gateway rodando na porta ${PORT}`);
  console.log(`📄 Swagger UI disponível em http://localhost:${PORT}/api-docs`);
});
