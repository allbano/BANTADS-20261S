FROM node:24-alpine AS builder

WORKDIR /app

# Instala dependências (inclui devDependencies para compilar TS)
COPY package*.json ./
RUN npm ci

# Copia código-fonte e compila TypeScript
COPY . .
RUN npm run build

# ─── Estágio de produção (sem devDependencies) ──────────────────────
FROM node:24-alpine

WORKDIR /app

COPY package*.json ./
RUN npm ci --omit=dev

# Copia apenas o código compilado do estágio anterior
COPY --from=builder /app/dist ./dist

EXPOSE 3000

CMD ["node", "dist/server.js"]