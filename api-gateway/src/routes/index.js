require("dotenv-safe").config();
const express = require('express');
const httpProxy = require('express-http-proxy');
const helmet = require('helmet');
const logger = require('morgan');
const cors = require('cors');
const verifyJWT = require('./middleware/auth'); 

const app = express();
const PORT = 40004; 

// URLs dos microsserviços no Docker 
const AUTH_URL = 'http://bantads-ms-auth:8085';
const CLIENTE_URL = 'http://ms-cliente:8080';
const CONTA_URL = 'http://ms-conta:8080';
const FUNCIONARIO_URL = 'http://ms-funcionario:8080';

app.use(logger('dev'));
app.use(helmet());
app.use(cors()); 
app.use(express.json());

/* ROTAS PÚBLICAS */
app.post('/login', httpProxy(AUTH_URL, {
    proxyReqPathResolver: () => '/auth/login' // Mapeia para o endpoint real do ms-auth
}));

app.post('/autocadastro', httpProxy(CLIENTE_URL, {
    proxyReqPathResolver: () => '/clientes/autocadastro'
}));

/* ROTAS PROTEGIDAS
 * Exigem o header "Authorization: Bearer XXXXX" 
 */
app.use('/clientes', verifyJWT, httpProxy(CLIENTE_URL));
app.use('/contas', verifyJWT, httpProxy(CONTA_URL));
app.use('/funcionarios', verifyJWT, httpProxy(FUNCIONARIO_URL));

app.listen(PORT, () => {
    console.log(`BANTADS API Gateway operando na porta ${PORT}`);
});