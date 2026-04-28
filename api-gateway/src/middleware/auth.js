const jwt = require('jsonwebtoken');

/**
 * Middleware para verificação de token JWT.
 * Valida a identidade do usuário antes de permitir que o API Gateway
 * repasse a requisição para os microsserviços internos.
 */
const verifyJWT = (req, res, next) => {
    // Recupera o header de autorização da requisição
    const authHeader = req.headers['authorization'];

    if (!authHeader) {
        return res.status(401).json({ 
            auth: false, 
            message: 'Acesso negado. Token não fornecido.' 
        });
    }

    // Deve seguir o padrão "Bearer <TOKEN>"
    const parts = authHeader.split(' ');
    if (parts.length !== 2 || parts[0] !== 'Bearer') {
        return res.status(401).json({ 
            auth: false, 
            message: 'Erro no formato do token.' 
        });
    }

    const token = parts[1];

    
    // A chave secreta deve ser idêntica à definida no ms-auth para que a validação funcione.

    const secret = process.env.JWT_SECRET || 'change-me-in-production';

    jwt.verify(token, secret, (err, decoded) => {
        if (err) {
            return res.status(401).json({ 
                auth: false, 
                message: 'Token inválido ou expirado.' 
            });
        }

        req.user = {
            id: decoded.id,
            tipo: decoded.tipo,
            login: decoded.login
        };
        
        next();
    });
};

module.exports = verifyJWT;