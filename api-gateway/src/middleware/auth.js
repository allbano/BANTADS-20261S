const jwt = require('jsonwebtoken');

function verifyJWT(req, res, next) {
    const token = req.headers['authorization']?.split(' ')[1]; // Formato "Bearer XXXXX" [cite: 193]

    if (!token) {
        return res.status(401).json({ auth: false, message: 'Nenhum token fornecido.' });
    }

    jwt.verify(token, JWT_SECRET, (err, decoded) => {
        if (err) {
            return res.status(401).json({ auth: false, message: 'Falha na autenticação do token.' });
        }
        
        // Guarda o ID ou perfil do utilizador para uso posterior, se necessário
        req.userId = decoded.id;
        req.userRole = decoded.role;
        next();
    });
}

const jwt = require('jsonwebtoken');

/**
 * Middleware para verificação de token JWT
 * Bloqueia acesso a funcionalidades sem login com sucesso 
 */
const verifyJWT = (req, res, next) => {
    // Extrair o header de autorização 
    const authHeader = req.headers['authorization'];

    if (!authHeader) {
        return res.status(401).json({ 
            auth: false, 
            message: 'Acesso negado. Token não fornecido.' 
        });
    }

    // Verificar o formato "Bearer <TOKEN>" 
    const parts = authHeader.split(' ');
    if (parts.length !== 2 || parts[0] !== 'Bearer') {
        return res.status(401).json({ 
            auth: false, 
            message: 'Erro no formato do token.' 
        });
    }

    const token = parts[1];

    // Validar o token usando a Secret configurada no ms-auth
    // A secret deve ser a mesma 
    const secret = process.env.JWT_SECRET || 'change-me-in-production';

    jwt.verify(token, secret, (err, decoded) => {
        if (err) {
            return res.status(401).json({ 
                auth: false, 
                message: 'Token inválido ou expirado.' 
            });
        }

        // Se válido, guarda os dados do utilizador no request e prossegue 
        req.user = {
            id: decoded.id,
            tipo: decoded.tipo,
            login: decoded.login
        };
        
        next();
    });
};

module.exports = verifyJWT;